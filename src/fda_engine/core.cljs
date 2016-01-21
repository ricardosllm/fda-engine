(ns fda-engine.core
  (:require [cljs-lambda.util :as lambda :refer [async-lambda-fn]]
            [cljs.reader :refer [read-string]]
            [cljs.nodejs :as nodejs]
            [cljs.core.async :as async]
            [s3-cljs.core])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

;; For optimizations :advanced
(set! *main-cli-fn* identity)

(def config
  (-> (nodejs/require "fs")
      (.readFileSync "static/config.edn" "UTF-8")
      read-string))

(defn download [bucket-name key]
  (go
    (<! (s3-cljs.core/get-object bucket-name key))
    (js/Error. (str "Failed to download object: " key))))

(def ^:export core
  (async-lambda-fn
    (fn [event context]
      (let [bucket (((first (event :Records)) :s3) :bucket)
            object (((first (event :Records)) :s3) :object)]
        (if (not= event (config :magic-word))
          (download (bucket :name) (object :key)))))))
