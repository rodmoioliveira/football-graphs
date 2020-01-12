(ns football.app
  (:require
   [shadow.resource :as rc]
   [utils.core :refer [assoc-pos]]
   [cljs.reader :as reader]
   [football.config :refer [config themes]]
   [football.draw-graph :refer [force-graph]]))

(def brazil-switzerland
  (-> (rc/inline "../data/graphs/brazil_switzerland,_1_1.edn") reader/read-string))

(def brazil-matches [brazil-switzerland])

(def matches-hash
  (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur))
          {}
          brazil-matches))

(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [el]
                             {:id (.getAttribute el "id")
                              :el el
                              :formation (-> el (.getAttribute "data-formation") keyword)
                              :data (-> el
                                        (.getAttribute "data-match-id")
                                        keyword
                                        matches-hash
                                        ((fn [v]
                                           (let [id (-> el (.getAttribute "data-team-id") keyword)]
                                             {:nodes (-> v :nodes id (assoc-pos el))
                                              :links (-> v :links id)}))))

                              :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

(defn init []
  (doseq [canvas all-canvas]
    (force-graph {:data (-> canvas :data clj->js)
                  :config (config {:id (canvas :id)
                                   :theme (-> canvas :theme themes)})})))

(defn reload! [] (init))
