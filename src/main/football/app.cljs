(ns football.app
  (:require
   [shadow.resource :as rc]
   [cljs.reader :as reader]

   [utils.core :refer [assoc-pos
                       set-canvas-dimensions
                       canvas-dimensions
                       mobile-mapping
                       hash-by
                       get-global-metrics]]
   [utils.dom :refer [plot-dom reset-dom toogle-theme-btn toogle-theme]]
   [football.metrics-nav :refer [select-metrics$ sticky-nav$]]
   [football.config :refer [config]]
   [football.draw-graph :refer [force-graph]]))

(set! *warn-on-infer* true)

; ==================================
; Matches
; ==================================
(def world-cup-matches
  [(-> (rc/inline "../data/analysis/argentina_croatia,_0_3.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/australia_peru,_0_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/belgium_japan,_3_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/belgium_panama,_3_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/brazil_belgium,_1_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/brazil_costa_rica,_2_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/brazil_switzerland,_1_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/colombia_england,_1_1_(_p).edn") reader/read-string)
   (-> (rc/inline "../data/analysis/costa_rica_serbia,_0_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/croatia_denmark,_1_1_(_p).edn") reader/read-string)
   (-> (rc/inline "../data/analysis/croatia_england,_2_1_(_e).edn") reader/read-string)
   (-> (rc/inline "../data/analysis/croatia_nigeria,_2_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/denmark_australia,_1_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/denmark_france,_0_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/egypt_uruguay,_0_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/england_belgium,_0_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/france_australia,_2_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/france_belgium,_1_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/germany_sweden,_2_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/iceland_croatia,_1_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/iran_portugal,_1_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/iran_spain,_0_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/japan_senegal,_2_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/nigeria_iceland,_2_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/portugal_morocco,_1_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/portugal_spain,_3_3.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/russia_croatia,_2_2_(_p).edn") reader/read-string)
   (-> (rc/inline "../data/analysis/russia_egypt,_3_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/russia_saudi_arabia,_5_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/saudi_arabia_egypt,_2_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/senegal_colombia,_0_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/serbia_brazil,_0_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/serbia_switzerland,_1_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/spain_russia,_1_1_(_p).edn") reader/read-string)
   (-> (rc/inline "../data/analysis/sweden_england,_0_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/sweden_korea_republic,_1_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/sweden_switzerland,_1_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/switzerland_costa_rica,_2_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/uruguay_france,_0_2.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/uruguay_portugal,_2_1.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/uruguay_russia,_3_0.edn") reader/read-string)
   (-> (rc/inline "../data/analysis/uruguay_saudi_arabia,_1_0.edn") reader/read-string)])

(def matches-hash
  (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur))
          {}
          world-cup-matches))

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
  [{:keys [scale]}]
  (-> js/document
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
                                                   ((fn [k] (if (mobile?) (mobile-mapping k) k))))
                                   nodes (-> v :nodes id (assoc-pos el orientation))]
                               (((set-canvas-dimensions scale) orientation) el)
                               {:match-id (-> v :match-id)
                                :nodes nodes
                                :canvas-dimensions (canvas-dimensions scale)
                                :orientation orientation
                                ; TODO: move hashs to preprocessing data..
                                :nodeshash (-> nodes
                                               ((fn [n]
                                                  (reduce (partial hash-by :id) (sorted-map) n))))
                                :links (-> v :links id)
                                :min-max-values (-> v :min-max-values)
                                :label (-> v :label)}))))}) %))))

(defn plot-graphs
  "Plot all data inside canvas."
  [{:keys [global-metrics?
           node-radius-metric
           node-color-metric
           matches
           get-global-metrics
           name-position
           scale
           min-passes-to-display
           theme-background
           theme-lines-color
           theme-font-color]}]
  (doseq [canvas (all-canvas {:scale scale})]
    (force-graph {:data (-> (merge (-> canvas :data) {:graphs-options
                                                      {:min-passes-to-display min-passes-to-display}
                                                      :field
                                                      {:background theme-background
                                                       :lines-color theme-lines-color
                                                       :lines-width 2}}) clj->js)
                  :config (config {:id (canvas :id)
                                   :node-radius-metric node-radius-metric
                                   :node-color-metric node-color-metric
                                   :name-position name-position
                                   :font-color theme-font-color
                                   :min-max-values (if global-metrics?
                                                     (get-global-metrics matches)
                                                     (-> canvas :data :min-max-values))})})))

(defn init
  "Init graph interations."
  []
  (let [metrics (select-metrics$)
        input$ (-> metrics :input$)
        click$ (-> metrics :click$)
        opts {:matches world-cup-matches
              :scale 9
              :get-global-metrics get-global-metrics
              :name-position :bottom}]
    (do
      (reset-dom)
      (plot-dom world-cup-matches)
      (sticky-nav$)
      (-> input$
          (.subscribe #(-> % (merge opts) plot-graphs)))
      (-> click$
          (.subscribe #(-> % (merge opts)
                           ((fn [{:keys [theme-text theme] :as obj}]
                              (do
                                (toogle-theme-btn theme-text)
                                (toogle-theme theme)
                                (plot-graphs obj))))))))))

(defn reload! [] (init))
