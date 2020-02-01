(ns football.metrics-nav
  (:require ["rxjs" :as rx]
            ["rxjs/operators" :as rx-op]))

(defn select-metrics$
  []
  (let [node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
        node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
        coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
        is-global? (fn [v] (= v :global))]
    (-> (rx/of node-color-select node-area-select coverage-select)
        (.pipe
          (rx-op/mergeMap #(-> (rx/fromEvent % "change")
                               (.pipe (rx-op/map
                                        (fn []
                                          {:node-color-metric (-> node-color-select .-value keyword)
                                           :node-radius-metric (-> node-area-select .-value keyword)
                                           :global-metrics? (-> coverage-select .-value keyword is-global?)})))))
          (rx-op/startWith {:node-color-metric (-> node-color-select .-value keyword)
                                           :node-radius-metric (-> node-area-select .-value keyword)
                                           :global-metrics? (-> coverage-select .-value keyword is-global?)})))))
