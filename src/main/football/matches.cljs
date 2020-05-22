(ns football.matches
  (:require
   [shadow.resource :as rc]
   [cljs.reader :as reader]))

(def files (-> (rc/inline "../data/analysis/filenames.edn") reader/read-string))

(def matches-files-hash
  (-> files :files-ids-hash))

(def labels-hash
  (-> files :files-labels-hash))
