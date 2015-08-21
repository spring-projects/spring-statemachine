(defproject spring-statemachine-jepsen "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:plot {:env {:plot "plot"}}}
  :plugins [[lein-environ "1.0.0"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.1.0"]
                 [environ "1.0.0"]
                 [jepsen "0.0.5"]])
