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

(defn sm-read-states
  "Reading states from a state machine"
  [node]
  (let [response (http/get (str "http://" (name node) ":8080/states") {:as :json})]
    (get response :body)))

(defn sm-read-status-ok?
  "Read status and check that there is no errors"
  [node]
  (let [response (http/get (str "http://" (name node) ":8080/status") {:as :json})]
    (= (get (get response :body) :hasStateMachineError) false)))

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
        (recur))))
  )

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
                  (if (= (sm-read-states client) (:s op))
                    (assoc op :type :ok)
                    (assoc op :type :fail :value "wrong states"))
                (catch RuntimeException e
                  (assoc op :type :fail :value (.getMessage e))))
        :event (try
                 (sm-send-event client (:e op))
                 (assoc op :type :ok)
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

(defn event-gen-1
  "Generates isolated event and checks states and status"
  []

  (gen/phases
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S1","S11"]})))
    ;pick random node for sending event C
    (gen/clients
      (gen/once {:type :invoke
                 :f    :event
                 :e    "C"}))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;check variable foo=0 for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :variable
                   :v    "foo"
                   :r    0})))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S2","S21","S211"]})))))

(defn event-gen-2
  "Generates parallel event and checks states and status"
  []

  (gen/phases
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S1","S11"]})))
    ;pick all nodes for sending event C
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :event
                   :e    "C"})))
    (gen/sleep 2)
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S2","S21","S211"]})))))

(defn event-gen-3
  "Generates event and checks states, status and variable"
  []

  (gen/phases
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S1","S11"]})))
    ;pick random node for sending event J with variable x1
    (gen/clients
      (gen/once {:type :invoke
                 :f    :eventvariable
                 :e    "J"
                 :v    "x1"}))
    (gen/sleep 2)
    ;check variable value x1 for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :variable
                   :v    "testVariable"
                   :r    "x1"})))))

(defn event-gen-4
  "Generates event and checks states while splitting network"
  []

  (gen/phases
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;start nemesis, split network
    (gen/nemesis
      (gen/once {:type :info :f :start}))
    (gen/sleep 30)
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;stop nemesis, heal network
    (gen/nemesis
      (gen/once {:type :info :f :stop}))
    ;get error status of all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :status})))
    ;pick random node for sending event C
    (gen/clients
      (gen/once {:type :invoke
                 :f    :event
                 :e    "C"}))
    ;check states for all machines
    (gen/clients
      (gen/each
        (gen/once {:type :invoke
                   :f    :states
                   :s    ["S0","S2","S21","S211"]})))))

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
                :generator (event-gen-1)}))

(defn send-parallel-event-test
  "Sends simple events via all nodes."
  []
  (event-test "send-parallel-event"
               {:nemesis   nemesis/noop
                :generator (event-gen-2)}))

(defn send-isolated-event-with-variable-test
  "Sends simple events via random node with variable."
  []
  (event-test "send-isolated-event-with-variable"
               {:nemesis   nemesis/noop
                :generator (event-gen-3)}))

(defn partition-half-test
  "Does a half brain split and checks that machines are healing."
  []
  (event-test "partition-half"
               {:nemesis   (nemesis/partition-random-halves)
                :generator (event-gen-4)}))
