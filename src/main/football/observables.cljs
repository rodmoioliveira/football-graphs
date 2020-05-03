(ns football.observables
  (:require
   ["rxjs" :as rx]
   ["rxjs/operators" :as rx-op]

   [utils.dom :refer [dom]]
   [mapping.themes :refer [theme-mapping
                           theme-identity
                           theme-reverse
                           get-theme-with]]))

(set! *warn-on-infer* true)

(defn select-metrics$
  []
  (let [display-passes (fn [{:keys [min-passes-to-display]}]
                         (set! (.-innerHTML (dom :min-passes-span)) (str "(" min-passes-to-display ")")))
        is-global? (fn [v] (= v :global))
        get-metrics (fn [] {:node-color-metric (-> dom :node-color-select .-value keyword)
                            :node-radius-metric (-> dom :node-area-select .-value keyword)
                            :min-passes-to-display (-> dom :min-passes-input .-value int)
                            :global-metrics? (-> dom :coverage-select .-value keyword is-global?)})
        current-theme (fn [] (-> dom :body-theme (.getAttribute "data-theme") keyword))
        input$ (-> (rx/of
                    (-> dom :node-color-select)
                    (-> dom :node-area-select)
                    (-> dom :coverage-select)
                    (-> dom :min-passes-input))
                   (.pipe
                    (rx-op/mergeMap #(-> (rx/fromEvent % "input")
                                         (.pipe (rx-op/map (fn [_]
                                                             (merge
                                                              (get-metrics)
                                                              (get-theme-with (partial theme-identity (current-theme)))))))))
                    (rx-op/startWith (merge
                                      (get-metrics)
                                      (get-theme-with (partial theme-identity (current-theme)))))
                    (rx-op/tap display-passes)))
        click$ (-> dom
                   :theme-btn
                   (rx/fromEvent "click")
                   (.pipe (rx-op/map (fn [_] (merge
                                              (get-metrics)
                                              (get-theme-with (partial theme-reverse (current-theme))))))))]
    {:input$ input$
     :click$ click$}))

(defn sticky-nav$
  []
  (let [activate-nav (fn [_] (-> dom :nav (.setAttribute "data-active" 1)))
        deactivate-nav (fn [_] (-> dom :nav (.setAttribute "data-active" 0)))
        is-body-click? (fn [e] (->> e
                                    .-path
                                    array-seq
                                    (map #(-> % .-tagName))
                                    set
                                    (#(or (contains? % "NAV") (contains? % "BUTTON")))
                                    not))]
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
          (.subscribe deactivate-nav)))))

(defn slider$
  []
  (let [slide-home (fn [_] (-> dom :slide-view (.setAttribute "data-view" "home")))
        slide-graph (fn [_] (-> dom :slide-view (.setAttribute "data-view" "graph")))]
    (do
      (-> dom :slide-to-graph
          (rx/fromEvent "click")
          (.subscribe slide-graph))

      (-> dom :slide-to-home
          (rx/fromEvent "click")
          (.subscribe slide-home)))))
