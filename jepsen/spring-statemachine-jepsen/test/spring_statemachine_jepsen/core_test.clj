(ns spring-statemachine-jepsen.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [spring-statemachine-jepsen.core :refer :all]
            [jepsen [core :as jepsen]
                    [report :as report]]))

(defn run-statemachine-test!
  "Runs a test around a state machine and dumps some results to the report/ dir"
  [t]
  (let [test (jepsen/run! t)]
    (or (is (:valid? (:results test)))
        (println (:error (:results test))))
    (report/to (str "report/" (:name test) "/history.edn")
               (pprint (:history test)))))

(deftest send-isolated-event
  (run-statemachine-test! (send-isolated-event-test)))

(deftest send-parallel-event
  (run-statemachine-test! (send-parallel-event-test)))

(deftest send-isolated-event-with-variable
  (run-statemachine-test! (send-isolated-event-with-variable-test)))

(deftest partition-half
  (run-statemachine-test! (partition-half-test)))

(deftest stop-start
  (run-statemachine-test! (stop-start-test)))
