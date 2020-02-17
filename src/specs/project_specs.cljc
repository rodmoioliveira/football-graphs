(ns project-specs
  (:require
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])))

(s/def ::int-or-double (s/or :int int? :double double?))
(s/def ::max ::int-or-double)
(s/def ::min ::int-or-double)
(s/def ::max-val-args (s/cat :values (s/coll-of ::max :min-count 1)))
(s/def ::min-val-args (s/cat :values (s/coll-of ::min :min-count 1)))
(s/def ::max-val-ret (s/keys :req-un [::max]))
(s/def ::min-val-ret (s/keys :req-un [::min]))
(s/def ::range (s/keys :req-un [::max ::min]))
(s/def ::ranges (s/cat :values (s/coll-of ::range :min-count 1)))

