(ns test.core
  (:require
   [clojure.spec.test.alpha :as stest]
   [clojure.set :refer [project]]
   [clojure.pprint :refer [pprint]]
   [utils.core]))

; TODO: automate clojurescript testing also...

(defn has-error?
  [results]
  (let [errors (reduce (fn [acc cur] (or (some? cur) acc)) false results)]
    (if errors
      (do
        (println (str (count results) " fail tests..."))
        (pprint results)
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

