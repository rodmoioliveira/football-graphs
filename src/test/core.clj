(ns test.core
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.set :refer [project]]
   [clojure.pprint :refer [pprint]]
   [utils.core]))

; TODO: automate clojurescript testing also...?
(defn has-error?
  [results]
  (let [errors (reduce (fn [acc cur] (or (some? cur) acc)) false results)
        num-errors (count results)
        pluralize? (not= num-errors 1)]
    (if errors
      (do
        (println)
        (println "================================")
        (println)
        (println (str num-errors " fail test" (when pluralize? "s")  "..."))
        (println)
        (pprint results)
        (println)
        (println "================================")
        (println)
        (throw (Exception. "Error"))
        (System/exit 0))
      (do
        (println "All tests passed!")
        (System/exit 0)))))

(-> (stest/enumerate-namespace 'utils.core)
    stest/check
    ((fn [tests] (map #(get-in % [:failure]) tests)))
    (#(filter some? %))
    (#(map Throwable->map %))
    (#(project % [:cause]))
    vec
    has-error?)

