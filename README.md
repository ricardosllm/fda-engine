# fda-engine

Face Detection App - Engine using AWS Lambda

## Deploying

Run `lein cljs-lambda default-iam-role` if you don't have yet have suitable
execution role to place in your project file.  This command will create an IAM
role under your default (or specified) AWS CLI profile, and modify your project
file to specify it as the execution default.

Otherwise, add an IAM role ARN under the function's `:role` key in the
`:functions` vector of your profile file, or in `:cljs-lambda` -> `:defaults` ->
`:role`.

Then:

```sh
$ lein cljs-lambda deploy
$ lein cljs-lambda invoke work-magic ...
```

## Testing

```sh
lein doo node fda-engine-test
```

Doo is provided to avoid including code to tell `cljs.test` to exit the Node
process after a test run.
