(ns fda-engine.test-runner
 (:require [doo.runner :refer-macros [doo-tests]]
           [fda-engine.core-test]
           [cljs.nodejs :as nodejs]))

(try
  (.install (nodejs/require "source-map-support"))
  (catch :default _))

(doo-tests
 'fda-engine.core-test)
