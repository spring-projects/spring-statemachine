(ns spring-statemachine-jepsen.checker
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging  :refer [info]]
            [environ.core :refer [env]]
            [jepsen.util :as util :refer [meh]]
            [jepsen.checker :refer [Checker]]
            [jepsen.store :as store]
            [knossos.op :as op]
            [knossos.history :as history]
            [gnuplot.core :as g]))

(def nodetovalue {"n1" 1 "n2" 2 "n3" 3 "n4" 4 "n5" 5 })
(def statetovalue {"S11" 1 "S12" 2 "S211" 3 "S212" 4 "error" 5 })
(def variabletovalue {"v1" 1 "v2" 2 "v3" 3 "v4" 4 "v5" 5 "v6" 6 "v7" 7 "v8" 8})

(defn shiftvalue
  [value process]
  (double (+ value (/ (- process 2) 10))))

(defn extract-plot-data
  [history]
  (let [data
    (->> history
         (filter #(= :ok (:type %)))
         (filter #(= :states (:f %)))
         (group-by :process))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 0))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 1))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 2))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 3))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 4)))))

(defn extract-plot-data2
  [history]
  (let [data
    (->> history
         (filter #(= :ok (:type %)))
         (filter #(= :event (:f %))))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (+ (get value :process) 1) (get value :value))) ) [] (vec data)))))

(defn extract-plot-data3
  [history]
  (let [data
    (->> history
         (filter #(= :ok (:type %)))
         (filter #(= :variable (:f %)))
         (group-by :process))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get variabletovalue (get value :r)) (get value :process) ))) ) [] (get data 0))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get variabletovalue (get value :r)) (get value :process) ))) ) [] (get data 1))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get variabletovalue (get value :r)) (get value :process) ))) ) [] (get data 2))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get variabletovalue (get value :r)) (get value :process) ))) ) [] (get data 3))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get variabletovalue (get value :r)) (get value :process) ))) ) [] (get data 4)))))

(defn extract-plot-data4
  [history]
  (let [data
    (->> history
         (filter #(= :ok (:type %)))
         (filter #(= :eventvariable (:f %))))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (+ (get value :process) 1) (get value :v))) ) [] (vec data)))))

(defn extract-plot-data5
  [history]
  (let [data
    (->> history
         (filter #(:value %))
         (filter #(= :nemesis (:process %)))
         (filter #(= :stop (:f %))))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (get nodetovalue (name (last (keys (get value :value))))) "X")) ) [] (vec data)))))

(defn extract-plot-data6
  [history]
  (let [data
    (->> history
         (filter #(= :ok (:type %)))
         (filter #(= :statesnoexpect (:f %)))
         (group-by :process))]
    (vector
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 0))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 1))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 2))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 3))
      (reduce-kv (fn [vec key value] (conj vec (vector (get value :time) (shiftvalue (get statetovalue (last (get value :value))) (get value :process) ))) ) [] (get data 4)))))

(defn plot1!
  [test model history]

  (let [output-path (.getCanonicalPath (store/path! test "states.png"))]
    (g/raw-plot! [[:set :key :outside]
                  [:set :style :textbox :opaque]
                  [:set :terminal :qt :size (keyword "900,450")]
                  [:set :yrange (keyword "[0.5:4.5]")]
                  [:set :y2range (keyword "[0.5:5.5]")]
                  [:set :xtics :format "%h\nns"]
                  [:set :xlabel "elapsed time"]
                  [:set :ylabel "states in nodes"]
                  [:set :y2label "events via nodes"]
                  [:set :ytics 1]
                  [:set :ytics (keyword "('S21' 1, 'S22' 2, 'S211' 3, 'S212' 4)")]
                  [:set :ytics :nomirror]
                  [:set :y2tics 1]
                  [:set :y2tics (keyword "('n1' 1, 'n2' 2, 'n3' 3, 'n4' 4, 'n5' 5)")]
                  [:plot
                   (g/list ["-" :title "states n1" :with :steps :lw :3]
                           ["-" :title "states n2" :with :steps :lw :3]
                           ["-" :title "states n3" :with :steps :lw :3]
                           ["-" :title "states n4" :with :steps :lw :3]
                           ["-" :title "states n5" :with :steps :lw :3]
                           ["-" :title "events" :with :labels :center :boxed :font ",15" :axis :x1y2]
                           )]]
                   (into
                     (extract-plot-data history)
                     (extract-plot-data2 history)))
    output-path)
    {:valid? true})

(defn plot2!
  [test model history]

  (let [output-path (.getCanonicalPath (store/path! test "states.png"))]
    (g/raw-plot! [[:set :key :outside]
                  [:set :style :textbox :opaque]
                  [:set :terminal :qt :size (keyword "900,450")]
                  [:set :yrange (keyword "[0.5:8.5]")]
                  [:set :y2range (keyword "[0.5:5.5]")]
                  [:set :xtics :format "%h\nns"]
                  [:set :xlabel "elapsed time"]
                  [:set :ylabel "variable in nodes"]
                  [:set :y2label "variable via nodes"]
                  [:set :ytics 1]
                  [:set :ytics (keyword "('v1' 1, 'v2' 2, 'v3' 3, 'v4' 4, 'v5' 5, 'v6' 6, 'v7' 7, 'v8' 8)")]
                  [:set :ytics :nomirror]
                  [:set :y2tics 1]
                  [:set :y2tics (keyword "('n1' 1, 'n2' 2, 'n3' 3, 'n4' 4, 'n5' 5)")]
                  [:plot
                   (g/list ["-" :title "variable n1" :with :steps :lw :3]
                           ["-" :title "variable n2" :with :steps :lw :3]
                           ["-" :title "variable n3" :with :steps :lw :3]
                           ["-" :title "variable n4" :with :steps :lw :3]
                           ["-" :title "variable n5" :with :steps :lw :3]
                           ["-" :title "variables" :with :labels :center :boxed :font ",15" :axis :x1y2]
                           )]]
                   (into
                     (extract-plot-data3 history)
                     (extract-plot-data4 history)))
    output-path)
    {:valid? true})

(defn plot3!
  [test model history]

  (let [output-path (.getCanonicalPath (store/path! test "states.png"))]
    (g/raw-plot! [[:set :key :outside]
                  [:set :style :textbox :opaque]
                  [:set :terminal :qt :size (keyword "900,450")]
                  [:set :yrange (keyword "[0.5:4.5]")]
                  [:set :y2range (keyword "[0.5:5.5]")]
                  [:set :xtics :format "%h\nns"]
                  [:set :xlabel "elapsed time"]
                  [:set :ylabel "states in nodes"]
                  [:set :y2label "crash/start in nodes"]
                  [:set :ytics 1]
                  [:set :ytics (keyword "('S21' 1, 'S22' 2, 'S211' 3, 'S212' 4)")]
                  [:set :ytics :nomirror]
                  [:set :y2tics 1]
                  [:set :y2tics (keyword "('n1' 1, 'n2' 2, 'n3' 3, 'n4' 4, 'n5' 5)")]
                  [:plot
                   (g/list ["-" :title "states n1" :with :steps :lw :3]
                           ["-" :title "states n2" :with :steps :lw :3]
                           ["-" :title "states n3" :with :steps :lw :3]
                           ["-" :title "states n4" :with :steps :lw :3]
                           ["-" :title "states n5" :with :steps :lw :3]
                           ["-" :title "crash" :with :labels :center :boxed :font ",15" :axis :x1y2]
                           )]]
                   (into
                     (extract-plot-data history)
                     (extract-plot-data5 history)))
    output-path)
    {:valid? true})

(defn plot4!
  [test model history]

  (let [output-path (.getCanonicalPath (store/path! test "states.png"))]
    (g/raw-plot! [[:set :key :outside]
                  [:set :style :textbox :opaque]
                  [:set :terminal :qt :size (keyword "900,450")]
                  [:set :yrange (keyword "[0.5:5.5]")]
                  [:set :y2range (keyword "[0.5:5.5]")]
                  [:set :xtics :format "%h\nns"]
                  [:set :xlabel "elapsed time"]
                  [:set :ylabel "states in nodes"]
                  [:set :y2label "events via nodes"]
                  [:set :ytics 1]
                  [:set :ytics (keyword "('S21' 1, 'S22' 2, 'S211' 3, 'S212' 4, 'error' 5)")]
                  [:set :ytics :nomirror]
                  [:set :y2tics 1]
                  [:set :y2tics (keyword "('n1' 1, 'n2' 2, 'n3' 3, 'n4' 4, 'n5' 5)")]
                  [:plot
                   (g/list ["-" :title "states n1" :with :steps :lw :3]
                           ["-" :title "states n2" :with :steps :lw :3]
                           ["-" :title "states n3" :with :steps :lw :3]
                           ["-" :title "states n4" :with :steps :lw :3]
                           ["-" :title "states n5" :with :steps :lw :3]
                           ["-" :title "events" :with :labels :center :boxed :font ",15" :axis :x1y2]
                           )]]
                   (into
                     (extract-plot-data6 history)
                     (extract-plot-data2 history)))
    output-path)
    {:valid? true})

(defn checker1
  "Constructs a Jepsen checker."
  []
  (reify Checker
    (check [_ test model history]
      (if (env :plot) (plot1! test model history) {:valid? true}))))

(defn checker2
  "Constructs a Jepsen checker."
  []
  (reify Checker
    (check [_ test model history]
      (if (env :plot) (plot2! test model history) {:valid? true}))))

(defn checker3
  "Constructs a Jepsen checker."
  []
  (reify Checker
    (check [_ test model history]
      (if (env :plot) (plot3! test model history) {:valid? true}))))

(defn checker4
  "Constructs a Jepsen checker."
  []
  (reify Checker
    (check [_ test model history]
      (if (env :plot) (plot4! test model history) {:valid? true}))))
