(ns fda-engine.core-test
  (:require [fda-engine.core :refer [core config]]
            [cljs.test :refer-macros [deftest is]]
            [cljs-lambda.util :refer [mock-context]]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(deftest download
  (is (= (config :src-bucket-url) "https://s3-ap-northeast-1.amazonaws.com/ricardosllm-fda/")))
