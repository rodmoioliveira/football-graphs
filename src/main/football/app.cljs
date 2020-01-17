(ns football.app
  (:require
   [shadow.resource :as rc]
   [cljs.reader :as reader]

   [utils.core :refer [assoc-pos set-canvas-dimensions mobile-mapping]]
   [football.config :refer [config themes]]
   [football.draw-graph :refer [force-graph]]))


; ==================================
; Test Graph
; ==================================
; (-> (rc/inline "../data/graphs/test.edn") reader/read-string clj->js js/console.log)

; ==================================
; Matches
; ==================================
(def brazil-matches
  [(-> (rc/inline "../data/graphs/brazil_switzerland,_1_1.edn") reader/read-string)
   (-> (rc/inline "../data/graphs/brazil_costa_rica,_2_0.edn") reader/read-string)
   (-> (rc/inline "../data/graphs/serbia_brazil,_0_2.edn") reader/read-string)])

(def matches-hash
  (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur))
          {}
          brazil-matches))

; ==================================
; Viewport
; ==================================
; TODO: apply RXjs to event resize
(defn mobile?
  []
  (< (-> js/window .-innerWidth) 901))

; ==================================
; Get canvas from DOM
; ==================================
(defn all-canvas
  [] (-> js/document
         (.querySelectorAll ".graph__canvas")
         array-seq
         (#(map (fn [el]
                  {:id (.getAttribute el "id")
                   :data (-> el
                             (.getAttribute "data-match-id")
                             keyword
                             matches-hash
                             ((fn [v]
                                (let [id (-> el (.getAttribute "data-team-id") keyword)
                                      orientation (-> el
                                                      (.getAttribute "data-orientation")
                                                      keyword
                                                      ((fn [k] (if (mobile?) (mobile-mapping k) k))))]
                                  ((set-canvas-dimensions orientation) el)
                                  {:match-id (-> v :match-id)
                                   :nodes (-> v :nodes id (assoc-pos el orientation))
                                   :links (-> v :links id)
                                   :label (-> v :label)}))))
                   :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

; ==================================
; Graphs Init
; ==================================
(defn init []
  (doseq [canvas (all-canvas)]
    (-> js/document
        (.querySelector (str "[data-match-id=" "'" (-> canvas :data :match-id) "'" "].graph__label"))
        (#(set! (.-innerHTML %) (-> canvas :data :label))))
    (force-graph {:data (-> canvas :data clj->js)
                  :config (config {:id (canvas :id)
                                   :max-passes (-> brazil-matches
                                                   (#(map :max-passes %))
                                                   (#(apply max %)))
                                   :theme (-> canvas :theme themes)})})))

(defn reload! [] (init))
