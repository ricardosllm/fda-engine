(ns fda-engine.core-test
  (:require [fda-engine.core :refer [detect-face config]]
            [cljs.test :refer-macros [deftest is]]
            [cljs-lambda.local :refer [invoke channel]]
            [promesa.core :as p :refer-macros [alet]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn with-promised-completion [p]
  (cljs.test/async
    done
    (-> p
        (p/catch (fn [e]
                   (println (.. e -stack))
                   (is (not e))))
        (p/then done))))

(defn with-some-error [p]
  (p/branch p
            #(is false "Expected error")
            (constantly nil)))

(deftest wrong-word
  (-> (invoke detect-face {:original-bucket "not the magic word"})
      with-some-error
      with-promised-completion))

(def delay-channel-req
  {:original-bucket (:original-bucket config)
   :spell           :delay-channel
   :msecs           2})

(deftest delay-channel-spell
  (-> (invoke detect-face delay-channel-req)
      (p/then #(is (= % {"waited" 2})))))

(deftest delay-channel-spell-go
  (cljs.test/async
    done
    (go
      (let [[tag response] (<! (channel detect-face delay-channel-req))]
        (is (= tag :succeed))
        (is (= response {"waited" 2})))
      (done))))

(deftest delay-fail-spell
  (-> (invoke detect-face
              {:original-bucket (:original-bucket config)
               :spell           :delay-fail
               :msecs           3})
      with-some-error
      with-promised-completion))
