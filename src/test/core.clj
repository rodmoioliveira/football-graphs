(ns test.core
  (:require
   [clojure.spec.test.alpha :as stest]
   [utils.core]))

(-> (stest/enumerate-namespace 'utils.core) stest/check println)

