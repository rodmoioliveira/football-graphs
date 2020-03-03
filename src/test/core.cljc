; TODO: check this!
; https://code.thheller.com/blog/shadow-cljs/2019/10/12/clojurescript-macros.html
; https://clojurescript.org/about/differences#_macros
; https://bauerspace.com/refer-a-macro-with-cljc/
; https://hackernoon.com/clojure-macros-lessons-from-unspoken-symbols-c4945d8ed8bf
; https://github.com/anmonteiro/lumo/blob/2b4bc09f768fc57164cebc99a90b95bdb1a26762/src/test/cljs_build/test_check/core_test.cljs
(ns test.core
  (:require
   #?(:clj [clojure.spec.test.alpha :as stest]
      :cljs [cljs.spec.test.alpha :as stest :include-macros true])
   [clojure.set :refer [project]]
   [clojure.pprint :refer [pprint]]
   [utils.core]))

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


