(ns football.metrics-nav
  (:require ["rxjs" :as rx]
            ["rxjs/operators" :as rx-op]

            [utils.dom :refer [dom]]
            [mapping.themes :refer [theme-mapping]]))

(set! *warn-on-infer* true)

(defn current-theme
  [] (-> dom :body-theme (.getAttribute "data-theme") keyword))

(defn get-theme-mapping
  [prop]
  (-> (current-theme) theme-mapping prop))

(defn is-global? [v] (= v :global))

(defn get-metrics
  [] {:node-color-metric (-> dom :node-color-select .-value keyword)
      :node-radius-metric (-> dom :node-area-select .-value keyword)
      :position-metric (-> dom :position-select .-value keyword)
      :min-passes-to-display (-> dom :min-passes-input .-value int)
      :global-metrics? (-> dom :coverage-select .-value keyword is-global?)})

(defn select-metrics$
  []
  (let [display-passes (fn [{:keys [min-passes-to-display]}]
                         (set! (.-innerHTML (dom :min-passes-span)) (str "(" min-passes-to-display ")")))]
    (-> (rx/of
         (-> dom :node-color-select)
         (-> dom :node-area-select)
         (-> dom :coverage-select)
         (-> dom :position-select)
         (-> dom :min-passes-input))
        (.pipe
         (rx-op/mergeMap #(-> (rx/fromEvent % "input")
                              (.pipe (rx-op/map get-metrics))))
         (rx-op/startWith (get-metrics))
         (rx-op/tap display-passes)))))

(defn sticky-nav$
  []
  (do
    (-> js/document
        (rx/fromEvent "scroll")
        (.pipe
         (rx-op/map (fn [] (-> dom :breakpoint (.getBoundingClientRect) .-top (#(if (neg? %) 1 0)))))
         (rx-op/distinctUntilChanged))
        (.subscribe (fn [v]
                      (do
                        (-> dom :menu (.setAttribute "data-sticky" v))))))

    (-> dom
        :theme-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_]
                      (do
                        (-> dom :theme-btn (#(set! (.-innerHTML %) (get-theme-mapping :text))))
                        (-> dom :body-theme
                            (.setAttribute "data-theme" (get-theme-mapping :theme)))))))

    (-> dom :activate-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_] (-> dom :nav (.setAttribute "data-active" 1)))))

    (-> dom :deactivate-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_] (-> dom :nav (.setAttribute "data-active" 0)))))))
