(ns spring-statemachine-jepsen.core
  (:require [cheshire.core          :as json]
            [clojure.java.io        :as io]
            [clojure.string         :as str]
            [clojure.tools.logging  :refer [info]]
            [jepsen [core      :as jepsen]
                    [db        :as db]
                    [util      :as util :refer [meh timeout]]
                    [control   :as c :refer [|]]
                    [client    :as client]
                    [checker   :as checker]
                    [model     :as model]
                    [generator :as gen]
                    [nemesis   :as nemesis]
                    [store     :as store]
                    [report    :as report]
                    [tests     :as tests]]
            [spring-statemachine-jepsen.checker :refer [checker1]]
            [spring-statemachine-jepsen.checker :refer [checker2]]
            [spring-statemachine-jepsen.checker :refer [checker3]]
            [spring-statemachine-jepsen.checker :refer [checker4]]
            [jepsen.checker.timeline  :as timeline]
            [jepsen.control.net       :as net]
            [jepsen.os.debian         :as debian]
            [clj-http.client          :as http]))

(def pidfile      "/var/run/sm.pid")
(def binary       "/usr/bin/java")
(def jar          "/root/spring-statemachine-samples-web-1.0.0.BUILD-SNAPSHOT.jar")

(defn sm-send-event
  "Sends event to state machine"
  [node event]
  (info "Sending event" event (name node))
  (http/post (str "http://" (name node) ":8080/event")
    {:form-params {:id (str event)}}))

(defn sm-send-event-with-variable
  "Sends event to state machine"
  [node event value]
  (info "Sending event" event (name node))
  (http/post (str "http://" (name node) ":8080/event")
    {:form-params {:id (str event) :testVariable value}}))

(defn sm-read-status-ok?
  "Read status and check that there is no errors"
  [node]
  (let [response (http/get (str "http://" (name node) ":8080/status") {:as :json})]
    (= (get (get response :body) :hasStateMachineError) false)))

(defn sm-read-states
  "Reading states from a state machine"
  [node]
  (if (sm-read-status-ok? node)
    (let [response (http/get (str "http://" (name node) ":8080/states") {:as :json})]
      (get response :body))
    (vec ["error"])))

(defn sm-read-state-variable
  "Read status and check that there is no errors"
  [node key]
  (let [response (http/get (str "http://" (name node) ":8080/status") {:as :json})]
    (get (get (get response :body) :extendedStateVariables) (keyword key))))

(defn running?
  "Is the service running?"
  []
  (try
    (c/exec :start-stop-daemon :--status
            :--pidfile pidfile)
    true
    (catch RuntimeException _ false)))

(defn wait
  "Waits sm to become healthy"
  [node timeout-secs]
  (timeout (* 1000 timeout-secs)
           (throw (RuntimeException.
                    (str "Timed out after "
                         timeout-secs
                         " s waiting for state machine become healty")))
    (loop []
      (when
        (try
          (Thread/sleep 1000)
          (if (sm-read-status-ok? node) false true)
          (catch Exception e true))
        (recur)))))

(defn start!
  [node]
  "Starts state machine."
  (info node "starting state machine")
  (c/su
    (assert (not (running?)))
    (meh (c/exec :rm :-rf "/tmp/spring.log"))
    (c/exec :start-stop-daemon :--start
            :--background
            :--make-pidfile
            :--pidfile  pidfile
            :--exec     binary
            :--
            :-jar       jar
            (c/lit "2>&1")))
  (info node "waiting state machine to start")
  (wait node 120)
  (info node "state machine ready"))

(defn stop!
  "Stops state machine."
  [node]
  (info node "stopping state machine")
  (c/exec :start-stop-daemon :--stop
          :--pidfile pidfile)
  (info node "waiting state machine to stop")
  (Thread/sleep (* 10 1000)))

(defn db
  "Spring Statemachine for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (doto node
        (start!)))

    (teardown! [_ test node]
      (stop! node)
      ;; Leave system up, to collect logs, analyze post mortem, etc
      )))

(defrecord CreateEventClient [client]
  client/Client
  (setup! [_ test node]
    (let []
      (CreateEventClient. node)))

  (invoke! [this test op]
      (case (:f op)
        :status (try
                  (if (sm-read-status-ok? client)
                    (assoc op :type :ok)
                    (assoc op :type :fail :value "sm in error"))
                (catch RuntimeException e
                  (assoc op :type :fail :value (.getMessage e))))
        :variable (try
                    (let [variable (sm-read-state-variable client (:v op))]
                      (if (= variable (:r op))
                        (assoc op :type :ok)
                        (assoc op :type :fail :value (str (:v op) " was " variable))))
                  (catch RuntimeException e
                    (assoc op :type :fail :value (.getMessage e))))
        :states (try
                  (let [res (sm-read-states client)]
                    (if (= res (:s op))
                      (assoc op :type :ok :value (vec res))
                      (assoc op :type :fail :value (str "wrong states " (pr-str res))))
                    )
                (catch RuntimeException e
                  (assoc op :type :fail :value (.getMessage e))))
        :statesnoexpect (try
                  (let [res (sm-read-states client)]
                      (assoc op :type :ok :value (vec res)))
                (catch RuntimeException e
                  (assoc op :type :fail :value (.getMessage e))))
        :event (try
                 (sm-send-event client (:e op))
                 (assoc op :type :ok :value (:e op))
               (catch RuntimeException e
                 (assoc op :type :fail :value (.getMessage e))))
        :eventvariable (try
                         (sm-send-event-with-variable client (:e op) (:v op))
                         (assoc op :type :ok)
                       (catch RuntimeException e
                         (assoc op :type :fail :value (.getMessage e))))
        ))

  (teardown! [_ test]
    (.close client)))

(defn create-event-client
  "A client sendind events"
  []
  (CreateEventClient. nil))

(defn gen-read-states
  "Read states n times and expect states."
  [times expect]
  (gen/clients
    (gen/each
      (gen/seq
        (take (* times 2)
          (cycle [(gen/sleep 1)
                  {:type :invoke
                   :f    :states
                   :s    expect}]))))))

(defn gen-read-states-noexpect
  "Read states n times."
  [times]
  (gen/clients
    (gen/each
      (gen/seq
        (take (* times 2)
          (cycle [(gen/sleep 1)
                  {:type :invoke
                   :f    :statesnoexpect}]))))))

(defn gen-send-event
  "Send event one time to random node."
  [event]
  (gen/clients
    (gen/once {:type :invoke
               :f    :event
               :e    event})))

(defn gen-send-event-all
  "Send event one time to all nodes."
  [event]
  (gen/clients
    (gen/each
      (gen/once {:type :invoke
                 :f    :event
                 :e    event}))))

(defn gen-send-event-variable
  "Send event with variable value to one node."
  [event variable]
  (gen/clients
    (gen/once {:type :invoke
               :f    :eventvariable
               :e    event
               :v    variable})))

(defn gen-read-variable
  "Read variable from all nodes and expect variable value."
  [expect]
  (gen/clients
    (gen/each
      (gen/seq
        (take 10
          (cycle [(gen/sleep 1)
                  {:type :invoke
                   :f    :variable
                   :v    "testVariable"
                   :r    expect}]))))))

(defn gen-status
  "Read states n times and expect states."
  [times]
  (gen/clients
    (gen/each
      (gen/seq
        (take (* times 2)
          (cycle [(gen/sleep 1)
                  {:type :invoke
                   :f    :status}]))))))

(defn event-gen-1
  "Generates isolated event and checks states and status"
  []
  (gen/phases
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event "I")
    (gen-read-states 5 ["S0","S1","S12"])
    (gen-send-event "C")
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen-send-event "I")
    (gen-read-states 5 ["S0","S2","S21","S212"])
    (gen-send-event "K")
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event "I")
    (gen-read-states 5 ["S0","S1","S12"])
    (gen-send-event "C")
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen-send-event "I")
    (gen-read-states 5 ["S0","S2","S21","S212"])
    (gen-send-event "K")
    (gen-read-states 5 ["S0","S1","S11"])))

(defn event-gen-2
  "Generates parallel event and checks states and status"
  []
  (gen/phases
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event-all "I")
    (gen-read-states 5 ["S0","S1","S12"])
    (gen-send-event-all "C")
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen-send-event-all "I")
    (gen-read-states 5 ["S0","S2","S21","S212"])
    (gen-send-event-all "K")
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event-all "I")
    (gen-read-states 5 ["S0","S1","S12"])
    (gen-send-event-all "C")
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen-send-event-all "I")
    (gen-read-states 5 ["S0","S2","S21","S212"])
    (gen-send-event-all "K")
    (gen-read-states 5 ["S0","S1","S11"])))

(defn event-gen-3
  "Generates event and checks states, status and variable"
  []
  (gen/phases
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event-variable "J" "v1")
    (gen-read-variable "v1")
    (gen-send-event-variable "J" "v2")
    (gen-read-variable "v2")
    (gen-send-event-variable "J" "v3")
    (gen-read-variable "v3")
    (gen-send-event-variable "J" "v4")
    (gen-read-variable "v4")
    (gen-send-event-variable "J" "v5")
    (gen-read-variable "v5")
    (gen-send-event-variable "J" "v6")
    (gen-read-variable "v6")
    (gen-send-event-variable "J" "v7")
    (gen-read-variable "v7")
    (gen-send-event-variable "J" "v8")
    (gen-read-variable "v8")))

(defn event-gen-4
  "Generates event and checks states while splitting network"
  []
  (gen/phases
    (gen-read-states-noexpect 10)
    (gen-send-event-all "C")
    (gen-read-states-noexpect 10)
    ;start nemesis, split network
    (gen/nemesis
      (gen/once {:type :info :f :start}))
    (gen-read-states-noexpect 15)
    ;stop nemesis, heal network
    (gen/nemesis
      (gen/once {:type :info :f :stop}))
    (gen-read-states-noexpect 100)
    (gen-send-event-all "K")
    (gen-read-states-noexpect 10)))

(defn event-gen-5
  "Generates starts and stops and checks joins"
  []
  (gen/phases
    (gen-read-states 5 ["S0","S1","S11"])
    (gen-send-event-all "C")
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 2 ["S0","S2","S21","S211"])
    (gen/nemesis
      (gen/seq [{:type :info :f :start}
                (gen/sleep 5)
                {:type :info :f :stop}]))
    (gen-read-states 5 ["S0","S2","S21","S211"])
    (gen-send-event-all "K")
    (gen-read-states 5 ["S0","S1","S11"])))

(defn killer
  "Kills statemachine on a random node on start, restarts it on stop."
  []
  (nemesis/node-start-stopper
    rand-nth
    (fn start [test node] (c/su (c/exec :pkill :-9 :-f :spring-statemachine-samples-web)))
    (fn stop  [test node] (start! node))))

(defn statemachine-test
  "Defaults for testing state machine."
  [name opts]
  (merge tests/noop-test
         {:name (str "statemachine-" name)
          :os   debian/os
          :db   (db "1.0.0")}
         opts))

(defn event-test
  "A generic create test."
  [name opts]
  (statemachine-test (str "event-" name)
           (merge {:client  (create-event-client)
                   :model   model/noop}
                  opts)))

(defn send-isolated-event-test
  "Sends simple events via random node."
  []
  (event-test "send-isolated-event"
               {:nemesis   nemesis/noop
                :generator (event-gen-1)
                :checker (checker1)}))

(defn send-parallel-event-test
  "Sends simple events via all nodes."
  []
  (event-test "send-parallel-event"
               {:nemesis   nemesis/noop
                :generator (event-gen-2)
                :checker (checker1)}))

(defn send-isolated-event-with-variable-test
  "Sends simple events via random node with variable."
  []
  (event-test "send-isolated-event-with-variable"
               {:nemesis   nemesis/noop
                :generator (event-gen-3)
                :checker (checker2)}))

(defn partition-half-test
  "Does a half brain split and checks that machines are healing."
  []
  (event-test "partition-half"
               {:nemesis   (nemesis/partition-random-halves)
                :generator (event-gen-4)
                :checker (checker4)}))

(defn stop-start-test
  "Stops and start nodes checking join is okk."
  []
  (event-test "partition-half"
               {:nemesis   (killer)
                :generator (event-gen-5)
                :checker (checker3)}))
