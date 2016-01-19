(ns fda-engine.core
  (:require [cljs-lambda.util :as lambda :refer [async-lambda-fn]]
            [cljs.reader :refer [read-string]]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; For optimizations :advanced
(set! *main-cli-fn* identity)

(def config
  (-> (nodejs/require "fs")
      (.readFileSync "static/config.edn" "UTF-8")
      read-string))

(defn download [bucket-name key]
  (print (str "bucket:  s3://" bucket-name "/" key)))

(def ^:export core
  (async-lambda-fn
    (fn [event context]
      (let [bucket (((first (event :Records)) :s3) :bucket)
            object (((first (event :Records)) :s3) :object)]
        (print context)
        (download (bucket :name) (object :key))
        (if (not= event (config :magic-word))
          ;; We can fail/succeed wherever w/ fail!/succeed! - we can also
          ;; leave an Error instance on the channel we return -
          ;; see :delayed-failure above.
          (lambda/fail! context "Your magic word is garbage"))))))



