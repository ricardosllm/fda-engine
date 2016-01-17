(defproject fda-engine "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "http://please.FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"]
                 [io.nervous/cljs-lambda "0.2.0"]
                 [io.nervous/cljs-nodejs-externs "0.2.0"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-npm "0.6.0"]
            [lein-doo "0.1.7-SNAPSHOT"]
            [io.nervous/lein-cljs-lambda "0.3.0"]]

  :npm {:dependencies [[source-map-support "0.2.8"]]}
  :source-paths ["src"]
  :cljs-lambda {:defaults      {:role "arn:aws:iam::820369952272:role/cljs-lambda-default"}
                :resource-dirs ["static"]
                ;:aws-profile "devfda"
                :functions
                               [{:name   "work-magic"
                                 :invoke fda-engine.core/work-magic}]}

  :cljsbuild {:builds [{:id           "fda-engine"
                        :source-paths ["src"]
                        :compiler     {:output-to     "target/fda-engine/fda_engine.js"
                                       :output-dir    "target/fda-engine"
                                       :target        :nodejs
                                       :optimizations :advanced}}
                       {:id           "fda-engine-test"
                        :source-paths ["src" "test"]
                        :compiler     {:output-to     "target/fda-engine-test/fda_engine.js"
                                       :output-dir    "target/fda-engine-test"
                                       :target        :nodejs
                                       :optimizations :none
                                       :main          fda-engine.test-runner}}]})
