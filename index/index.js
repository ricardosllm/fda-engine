var cv = require('opencv');
var crypto = require('crypto');
var AWS = require('aws-sdk');
var async = require('async');
var fs = require('fs');
var COLOR = [0, 255, 0]; // default red
var thickness = 2; // default 1
var defaultCameraId = "face-detection";
var config = require('./config');
var s3burl = config.s3Burl;

exports.handler = function(event, context) {
  var srcBucket = event.Records[0].s3.bucket.name;
  var region = event.Records[0].awsRegion;
  AWS.config.update({"region": region});
  console.log('Region changed to:' + region);
  var srcKey    = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));
  var s3 = new AWS.S3();
  var db = new AWS.DynamoDB();

  var dstBucket = srcBucket;
  var dstKey    = srcKey.replace('input/','output/');

  var cameraid = "";
  var rscounter = {
    bytecountraw : 0,
    itemcountraw : 0,
    bytecountcrop : 0,
    itemcountcrop : 0,
    execstarttime : 0
  };
  rscounter.execstarttime = (new Date()).getTime();

  // Download the image from S3, transform, and upload to a different S3 bucket.
  async.waterfall([
    function download(next) {
      console.log('Downloading picture from S3');
      // Download the image from S3 into a buffer.
      s3.getObject({
        Bucket: srcBucket,
        Key: srcKey
      }, next);
    },
    function save(response,next) {
      console.log('Save file to buffer');
      fs.writeFile('/tmp/image', response.Body, 'base64', next);
      cameraid = response.Metadata["cameraid"] ? response.Metadata["cameraid"]: defaultCameraId ;
      rscounter.itemcountraw++;
      rscounter.bytecountraw = response.ContentLength;

    },
    function detect(next) {
      console.log('Detect faces');
      cv.readImage('/tmp/image', function(err, im) {
        if (err) console.log(err);
        if (im.width() < 1 || im.height() < 1) throw new Error('Image has no size');

        im.detectObject('index/node_modules/opencv/data/haarcascade_frontalface_alt2.xml', {}, function(err, faces) {
          if (err) throw err;
          var files = [];
          for (var i = 0; i < faces.length; i++) {
            face = faces[i];
            //im.rectangle([face.x, face.y], [face.width, face.height], COLOR, 2);
            img_crop = im.crop(face.x-10, face.y-10,face.width+20, face.height+20);
            img_crop.save('/tmp/image-crop-'+i+'.png');
            files.push({'id':i,'file':'/tmp/image-crop-'+i+'.png'});
          }
          //im.save('/tmp/image-crop.png');
          next(null,files);
        });

      });
    },
          function upload(files,next) {
            console.log('Uploading detected faces to S3');
            var i = 0;
            if(!files.length)
              next(null);
            else
            {
              files.forEach(function(crop) {
                fs.readFile(crop.file, function (err, data) {
                  if (err) console.log(err);
                  s3.putObject({
                    Bucket: dstBucket,
                    Key: dstKey+'-'+crop.id+'.png',
                    Body: data,
                    ContentType: 'image/png',
                    ACL: "public-read",
                    Metadata: { cameraid:cameraid}
                  },function() {
                    i++;
                    rscounter.itemcountcrop++;
                    var fsstats = fs.statSync(crop.file);
                    var fileSizeInBytes = fsstats["size"];
                    rscounter.bytecountcrop += fileSizeInBytes;
                    if (i>=files.length*2) next(null);
                  });

                });
                var params = {
                  Item: {
                    id : {S: crypto.randomBytes(20).toString('hex')},
                    unixtimestamp: {N: Math.floor(new Date() / 1000).toString()},
                    cameraid: {S: cameraid},
                    croppedurl: {S: s3burl+dstKey+'-'+crop.id+'.png'},
                    rawurl: {S: s3burl+srcKey}
                  },
                  TableName: 'snapshot'
                };
                db.putItem(params, function(err, data) {
                  if (err) console.log(err);
                  i++;
                  if (i>=files.length*2) next(null);
                });


              });

            }

          },
          function updaterawcount(next){
            console.log('Uploading raw count ');

            var params = {
              "TableName": "resources-counter",
              "Key": {
                "id": {
                  "S": cameraid
                }
              },
              "UpdateExpression": "ADD bytecountraw :val1, itemcountraw :val2, bytecountcrop :val3, itemcountcrop :val4, exectime :val5",

              "ExpressionAttributeValues": {
                ":val1": {"N": rscounter.bytecountraw},
                ":val2": {"N": "" + rscounter.itemcountraw },
                ":val3": {"N": "" + rscounter.bytecountcrop },
                ":val4": {"N": "" + rscounter.itemcountcrop },
                ":val5": {"N": "" + ((new Date()).getTime() - rscounter.execstarttime)}
              },
              "ReturnValues": "ALL_NEW"
            };
            db.updateItem(params, function(err, data) {
              console.log('Uploading raw count completed');

              if (err) console.log(err);
              else console.log(data);

              next(null);

            });

          }
          ], function (err) {
            if (err) console.log(err);
            context.done();
          }
                 );
};
