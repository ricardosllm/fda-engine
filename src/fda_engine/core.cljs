(ns fda-engine.core
  (:require [cljs.nodejs :as nodejs]
            ;[cljs-lambda.context :as ctx]
            ;[cljs-lambda.macros :refer-macros [deflambda]]
            ;[cljs-lambda.util :as lambda]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; For optimizations :advanced
(set! *main-cli-fn* identity)

(def config
 (-> (nodejs/require "fs")
     (.readFileSync "static/config.edn" "UTF-8")
     read-string))

; (defmethod download :delay-fail
;  [{:keys [msecs] :or {msecs 1000}} ctx]
;  (go
;    (<! (async/timeout msecs))
;    ;; We can fail/succeed wherever w/ fail!/succeed! - we can also
;    ;; leave an Error instance on the channel we return, or return a reject
;    ;; promised - see :delayed-failure above.
;    (ctx/fail! ctx (js/Error. (str "Failing after " msecs " milliseconds")))))

; entry function puts on download channel
; download function gets from download channel and download the image from S3
; download function puts it on the save channel

; save function gets job from save channel and saves the photo to S3
; save function puts a job on the detect channel

; detect funtion gets the job from the detect channel and runs face detection algorithm
; detect function puts a upload job on the upload channel

; upload function gets the job from the upload channel and uploads the photo to S3
; upload function puts a done msg on the finish channel

; finish function gets the done msg from the finish channel and calls context.done

; Channels:
  ; download
  ; save
  ; detect
  ; upload
  ; complete

; Functions:
  ; download
  ; save
  ; detect
  ; upload
  ; complete
