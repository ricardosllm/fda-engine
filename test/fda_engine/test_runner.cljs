(ns fda-engine.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [fda-engine.core-test]))

(doo-tests
  'fda-engine.core-test)
