(ns football.app
  (:require
   [utils.core :refer [assoc-pos
                       set-canvas-dimensions
                       canvas-dimensions
                       mobile-mapping
                       hash-by
                       get-global-metrics]]
   [utils.dom :refer [plot-matches-list
                      reset-dom
                      slide-graph
                      fix-nav
                      set-collapse
                      fix-back
                      toogle-theme-btn
                      plot-dom
                      toogle-theme
                      dom]]

   [football.observables :refer [select-metrics$
                                 sticky-nav$
                                 slider$]]
   [football.matches :refer [world-cup-matches matches-hash]]
   [football.config :refer [config]]
   [football.draw-graph :refer [force-graph]]))

(set! *warn-on-infer* true)

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
        list$ (-> metrics :list$)
        opts {:matches world-cup-matches
              :scale 9
              :get-global-metrics get-global-metrics
              :name-position :bottom}]
    (do
      (reset-dom)
      (plot-matches-list world-cup-matches)
      (sticky-nav$)
      (slider$)
      (-> list$
          (.subscribe (fn [obj]
                        (do
                          (slide-graph)
                          (fix-back 1)
                          (fix-nav 1)
                          (set-collapse (-> dom :slider-home) 1)
                          (set-collapse (-> dom :slider-graph) 0)
                          (-> matches-hash (get-in [(obj :select-match)]) vector plot-dom)
                          (-> obj (merge opts) plot-graphs)))))
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
