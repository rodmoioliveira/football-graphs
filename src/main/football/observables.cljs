(ns football.observables
  (:require
   ["rxjs" :as rx]
   ["rxjs/operators" :as rx-op]

   [utils.dom :refer [dom
                      slide-home
                      is-body-click?
                      fix-nav
                      scroll-top
                      scroll-to-current-match
                      fix-back
                      activate-nav
                      deactivate-nav
                      set-collapse]]
   [mapping.themes :refer [theme-mapping
                           theme-identity
                           theme-reverse
                           get-theme-with]]))

(set! *warn-on-infer* true)

(defn select-metrics$
  []
  (let [display-passes (fn [{:keys [min-passes-to-display]}]
                         (set! (.-innerHTML (dom :min-passes-span)) (str "(" min-passes-to-display ")")))
        get-metrics (fn [] {:node-color-metric (-> dom :node-color-select .-value keyword)
                            :node-radius-metric (-> dom :node-area-select .-value keyword)
                            :min-passes-to-display (-> dom :min-passes-input .-value int)})
        current-theme (fn [] (-> dom :body-theme (.getAttribute "data-theme") keyword))
        input$ (-> (rx/of
                    (-> dom :node-color-select)
                    (-> dom :node-area-select)
                    (-> dom :min-passes-input))
                   (.pipe
                    (rx-op/mergeMap #(-> (rx/fromEvent % "input")
                                         (.pipe (rx-op/map (fn [_]
                                                             (merge
                                                              (get-metrics)
                                                              (get-theme-with (partial theme-identity (current-theme)))))))))
                    (rx-op/tap display-passes)))
        list$ (-> dom
                  :matches-list
                  (rx/fromEvent "click")
                  (.pipe
                   (rx-op/filter (fn [e] (-> e .-target (.hasAttribute "data-match-id"))))
                   (rx-op/map (fn [e] (-> e .-target (.getAttribute "data-match-id") keyword)))
                   (rx-op/map (fn [match-id]
                                (merge
                                 {:select-match match-id}
                                 (get-metrics)
                                 (get-theme-with (partial theme-identity (current-theme))))))))
        click$ (-> dom
                   :theme-btn
                   (rx/fromEvent "click")
                   (.pipe (rx-op/map (fn [_] (merge
                                              (get-metrics)
                                              (get-theme-with (partial theme-reverse (current-theme))))))))]
    {:input$ input$
     :click$ click$
     :list$ list$}))

(defn sticky-nav$
  []
  (do
    (-> dom :activate-btn
        (rx/fromEvent "click")
        (.subscribe activate-nav))

    (-> dom :document
        (rx/fromEvent "click")
        (.pipe
         (rx-op/filter is-body-click?))
        (.subscribe deactivate-nav))

    (-> dom :deactivate-btn
        (rx/fromEvent "click")
        (.subscribe deactivate-nav))))

(defn slider$
  []
  (do
    (-> dom :slide-to-home
        (rx/fromEvent "click")
        (.subscribe (fn [_] (do
                              (slide-home)
                              (fix-back 0)
                              (fix-nav 0)
                              (set-collapse (-> dom :slider-home) 0)
                              (set-collapse (-> dom :slider-graph) 1)
                              (scroll-to-current-match)))))))
