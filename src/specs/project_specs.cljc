(ns project-specs
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])))

(s/def ::coord (s/or :int int? :double double?))
(s/def ::point (s/cat :num1 ::coord :num2 ::coord))
(s/def ::distance (s/or :double double? :int int?))
