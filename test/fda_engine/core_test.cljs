(ns fda-engine.core-test
  (:require [fda-engine.core :refer [core download config]]
            [cljs.test :refer-macros [deftest is]]
            [cljs-lambda.util :refer [mock-context]]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;(deftest foo
;  (is (= 1 1)))
;

(deftest download
  (cljs.test/async
    done
    (go
      (let [[tag result] (<! (download
                               {:bucke-name "bucke-name"
                                :key        "key"}
                               (mock-context)))]
        (is (= tag :fail))
        (is (instance? js/Error result))
        (done)))))
