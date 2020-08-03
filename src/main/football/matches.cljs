(ns football.matches
  (:require
   [shadow.resource :as rc]
   [clojure.set :refer [project]]
   [cljs.reader :as reader]))

(def files (-> (rc/inline "../data/analysis/filenames.edn") reader/read-string))

(def matches-files-hash
  (-> files :files-ids-hash))

(def labels-hash
  (-> files :files-labels-hash))

(def search-pool
  (->
   labels-hash
   vals
   (project [:filename :label])
   vec))
