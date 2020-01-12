(ns football.app
  (:require
   [shadow.resource :as rc]
   [utils.core :refer [assoc-pos]]
   [cljs.reader :as reader]
   [football.data :refer [brazil switzerland]]
   [mapping.tatical :refer [tatical-schemes]]
   [football.config :refer [config themes]]
   [football.draw-graph :refer [force-graph]]))

(def brazil-switzerland
  (-> (rc/inline "../data/graphs/brazil_switzerland,_1_1.edn") reader/read-string))

(def all-matches
  (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur))
          {}
          [brazil-switzerland]))

; TODO: remove
(def teams
  {:brazil brazil
   :switzerland switzerland})

(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [el]
                             {:id (.getAttribute el "id")
                              :el el
                              :formation (-> el (.getAttribute "data-formation") keyword)
                              :data (-> el
                                        (.getAttribute "data-team")
                                        keyword
                                        teams)
                              :data-2 (-> el
                                          (.getAttribute "data-match-id")
                                          keyword
                                          all-matches
                                          ((fn [v]
                                             (let [id (-> el (.getAttribute "data-team-id") keyword)]
                                               {:nodes (-> v :nodes id)
                                                :links (-> v :links id)}))))

                              :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

; TODO: remove
(defn set-nodes-pos
  [[{:keys [nodes links]} canvas formation tatical-schemes]]
  {:links links
   :nodes (assoc-pos
           canvas
           nodes
           formation
           tatical-schemes)})

(defn init []
  (doseq [canvas all-canvas]
    (let [formation (-> canvas :formation)
          el (-> canvas :el)
          data (-> canvas :data)
          format-data (-> [data el formation tatical-schemes] set-nodes-pos clj->js)
          format-data-2 (-> canvas :data-2)]

      (force-graph {:data format-data
                    :data-2 format-data-2
                    :config (config {:id (canvas :id)
                                     :theme (-> canvas :theme themes)})}))))

(defn reload! [] (init))
