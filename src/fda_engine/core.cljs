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

(defn download [input]
  (print input))

(def ^:export core
  (async-lambda-fn
   (fn [{:keys [magic-word] :as input} context]
     (print (config :src-bucket-url))
     (print context)
     (if (not= magic-word (config :magic-word))
       ;; We can fail/succeed wherever w/ fail!/succeed! - we can also
       ;; leave an Error instance on the channel we return -
       ;; see :delayed-failure above.
       (lambda/fail! context "Your magic word is garbage")
       (download input)))))
