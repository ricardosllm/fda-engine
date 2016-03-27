(ns fda-engine.core-test
  (:require [fda-engine.core :refer [fda-engine config]]
            [cljs.test :refer-macros [deftest is]]
            [cljs-lambda.local :refer [invoke channel]]
            [promesa.core :as p :refer-macros [alet]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn with-some-error [p]
  (p/branch p
            #(is false "Expected error")
            (constantly nil)))

(deftest wrong-word
  (-> (invoke fda-engine {:original-bucket "not the magic word"})
      with-some-error))

(def delay-channel-req
  {:original-bucket (:original-bucket config)
   :spell           :delay-channel
   :msecs           2})

(deftest delay-channel-spell
  (-> (invoke fda-engine delay-channel-req)
      (p/then #(is (= % {"waited" 2})))))

(deftest delay-channel-spell-go
  (cljs.test/async
    done
    (go
      (let [[tag response] (<! (channel fda-engine delay-channel-req))]
        (is (= tag :succeed))
        (is (= response {"waited" 2})))
      (done))))

(def download-req
  {:original-bucket (:original-bucket config)
   :in-channel      :download-channel
   :out-channel     :save-channel})

(deftest download
  (cljs.test/async
    done
    (go
      (let [ [tag response] (<! (channel fda-engine download-req))]
        (is (= tag :succeed))
        (is (= response {"downloaded" :original-bucket})))
      (done))))