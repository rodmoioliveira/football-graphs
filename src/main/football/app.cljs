(ns football.app
  (:require
   [utils.core :refer [assoc-pos
                       set-canvas-dimensions
                       canvas-dimensions
                       normalize-filename
                       mobile-mapping
                       hash-by]]
   [utils.dom :refer [reset-dom
                      ; plot-matches-list
                      slide-graph
                      slide-home
                      loader-element
                      get-metrics
                      reset-hash!
                      scroll-to-current-match
                      get-current-theme
                      is-mobile?
                      fix-nav
                      scroll-top
                      set-collapse
                      fix-back
                      toogle-theme-btn
                      plot-dom
                      toogle-theme
                      fetch-file
                      set-hash!
                      get-hash
                      dom]]

   [football.observables :refer [select-metrics$
                                 sticky-nav$
                                 slider$]]
   [mapping.themes :refer [theme-identity
                           get-theme-with]]
   [football.matches :refer [matches-files-hash labels-hash]]
   [football.store :refer [store
                           update-store
                           theme-store
                           flush-simulations!
                           restart-simulations
                           stop-simulations]]
   [football.config :refer [config]]
   [football.draw-graph :refer [force-graph]]))

(set! *warn-on-infer* true)

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
                          ((fn [k] (-> @store (get-in [k]))))
                          ((fn [v]
                             (let [id (-> el (.getAttribute "data-team-id") keyword)
                                   orientation (-> el
                                                   (.getAttribute "data-orientation")
                                                   keyword
                                                   ((fn [k] (if (is-mobile?) (mobile-mapping k) k))))
                                   nodes (-> v :nodes id (assoc-pos el orientation))]
                               (((set-canvas-dimensions scale) orientation) el)
                               {:match-id (-> v :match-id)
                                :nodes nodes
                                :canvas-dimensions (canvas-dimensions scale)
                                :orientation orientation
                                :nodeshash (-> nodes
                                               ((fn [n]
                                                  (reduce (partial hash-by :id) (sorted-map) n))))
                                :links (-> v :links id)
                                :team-stats (-> v
                                                :match-info
                                                :home-away-id
                                                id
                                                ((fn [k] (get-in (-> v :stats) [k]))))
                                :global-stats (-> v :stats :global)
                                :label (-> v :label)}))))}) %))))

(defn plot-graphs
  "Plot all data inside canvas."
  [{:keys [node-radius-metric
           node-color-metric
           name-position
           scale
           mobile?
           min-passes-to-display
           theme-background
           theme-lines-color
           theme-font-color
           theme-edge-color-range
           theme-node-color-range
           theme-outline-node-color]}]
  (doseq [canvas (all-canvas {:scale scale})]
    (force-graph {:data (-> (merge (-> canvas :data)
                                   {:graphs-options
                                    {:min-passes-to-display min-passes-to-display}
                                    :field
                                    {:background theme-background
                                     :lines-color theme-lines-color
                                     :lines-width 2}}) clj->js)
                  :config (config {:id (canvas :id)
                                   :node-radius-metric node-radius-metric
                                   :node-color-metric node-color-metric
                                   :outline-node-color theme-outline-node-color
                                   :name-position name-position
                                   :font-color theme-font-color
                                   :mobile? mobile?
                                   :home-or-away? (-> canvas :data :home-or-away?)
                                   :node-color-range theme-node-color-range
                                   :edge-color-range theme-edge-color-range
                                   :global-stats (-> canvas :data :global-stats)
                                   :team-stats (-> canvas :data :team-stats)})})))

(defn init
  "Init graph interations."
  [dev?]
  (let [url-match-id (-> labels-hash (get-in [(get-hash) :match-id]))
        metrics (select-metrics$)
        dev-reload? (-> dev? (= :development))
        input$ (-> metrics :input$)
        toogle-theme$ (-> metrics :toogle-theme$)
        list$ (-> metrics :list$)
        opts {:mobile? (is-mobile?)
              :scale 9
              :name-position :bottom}
        apply-hash (fn [data]
                     (-> data
                         :match-id
                         str
                         keyword
                         matches-files-hash
                         :filename
                         normalize-filename
                         set-hash!))]
    (when-not dev-reload?
      (do
        (reset-dom)
        (sticky-nav$)
        (-> slider$
            (.subscribe (fn [_]
                          (reset-hash!)
                          (slide-home)
                          (fix-back 0)
                          (fix-nav 0)
                          (set-collapse (-> dom :slider-home) 0)
                          (set-collapse (-> dom :slider-graph) 1)
                          (scroll-to-current-match)
                          (stop-simulations)
                          (flush-simulations!))))
        ; (->> matches-files-hash
        ;      vals
        ;      (filter #(-> % :championship (= "france")))
        ;      (group-by :year)
        ;      vals
        ;      ; first
        ;      clj->js
        ;      js/console.log)
        ; (plot-matches-list
        ;  (-> js/document (.querySelector (str "[data-championship='france-2018']")))
        ;  (->> matches-files-hash
        ;       vals
        ;       (filter #(-> % :championship (= "france")))
        ;       (group-by :year)
        ;       vals
        ;       second
        ;       (sort-by :label)))
        (-> toogle-theme$
            (.subscribe #(-> % (merge opts)
                             ((fn [{:keys [theme-text theme]}]
                                (toogle-theme-btn theme-text)
                                (toogle-theme theme)
                                (restart-simulations))))))
        (-> input$
            (.subscribe (fn [_] (restart-simulations))))

        (-> list$
            (.subscribe
             (fn [obj]
               (slide-graph (-> obj :select-match name))
               (fix-back 1)
               (fix-nav 1)
               (scroll-top)
               (set-collapse (-> dom :slider-home) 1)
               (set-collapse (-> dom :slider-graph) 0)
               (-> matches-files-hash
                   (get-in [(-> obj :select-match)])
                   ((fn [{:keys [filename match-id]}]
                      (let [store-data (get-in @store [(-> match-id str keyword)])]
                        (if store-data
                          (do
                            (apply-hash store-data)
                            (-> store-data
                                vector
                                ((fn [d]
                                   (plot-dom d)
                                   (-> obj (merge opts) plot-graphs)))))
                          (do
                            (-> dom :plot-section (#(set! (.-innerHTML %) loader-element)))
                            (fetch-file
                             filename
                             [update-store
                              apply-hash
                              (fn [d] (-> d vector plot-dom))
                              (fn [] (-> obj (merge opts) plot-graphs))]))))))))))
          ; routing
        (when url-match-id
          (-> dom
              :matches-lists
              (.querySelector (str "[data-match-id='" url-match-id "']"))
              (.click)))))

    (when dev-reload?
      (->
       (merge
        (get-metrics)
        (get-theme-with (partial theme-identity (get-current-theme)))
        opts)
       ((fn [{:keys [theme-text theme] :as obj}]
          (toogle-theme-btn theme-text)
          (toogle-theme theme)
          (plot-graphs obj)))))))

(defn reload! [] (init :development))
