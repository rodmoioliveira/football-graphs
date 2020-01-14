(ns football.app
  (:require
   [shadow.resource :as rc]
   [utils.core :refer [assoc-pos]]
   [cljs.reader :as reader]
   [football.config :refer [config themes]]
   [football.draw-graph :refer [force-graph]]))

; ==================================
; Matches
; ==================================
(def brazil-matches
  [(-> (rc/inline "../data/graphs/brazil_switzerland,_1_1.edn") reader/read-string)])

(def matches-hash
  (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur))
          {}
          brazil-matches))

; ==================================
; Get canvas from DOM
; ==================================
(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [el]
                             {:id (.getAttribute el "id")
                              :data (-> el
                                        (.getAttribute "data-match-id")
                                        keyword
                                        matches-hash
                                        ((fn [v]
                                           (let [id (-> el (.getAttribute "data-team-id") keyword)]
                                             {:match-id (-> v :match-id)
                                              :nodes (-> v :nodes id (assoc-pos el))
                                              :links (-> v :links id)
                                              :label (-> v :label)}))))
                              :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

; ==================================
; Graphs Init
; ==================================
(defn init []
  (doseq [canvas all-canvas]
    (-> js/document
        (.querySelector (str "[data-match-id=" "'" (-> canvas :data :match-id) "'" "].graph__label"))
        (#(set! (.-innerHTML %) (-> canvas :data :label))))
    (force-graph {:data (-> canvas :data clj->js)
                  :config (config {:id (canvas :id)
                                   :theme (-> canvas :theme themes)})})))

(defn reload! [] (init))
