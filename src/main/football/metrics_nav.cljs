(ns football.metrics-nav
  (:require ["rxjs" :as rx]
            ["rxjs/operators" :as rx-op]))

(set! *warn-on-infer* true)

(defn select-metrics$
  []
  (let [node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
        node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
        coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
        position-select (-> js/document (.querySelector (str "[data-metric='position']")))
        is-global? (fn [v] (= v :global))
        get-metrics (fn [] {:node-color-metric (-> node-color-select .-value keyword)
                        :node-radius-metric (-> node-area-select .-value keyword)
                        :position-metric (-> position-select .-value keyword)
                        :global-metrics? (-> coverage-select .-value keyword is-global?)})]
    (-> (rx/of node-color-select node-area-select coverage-select position-select)
        (.pipe
         (rx-op/mergeMap #(-> (rx/fromEvent % "change")
                              (.pipe (rx-op/map get-metrics))))
         (rx-op/startWith (get-metrics))))))

(defn sticky-nav$
  []
  (let [nav (-> js/document (.querySelector ".nav-metrics"))
        nav-height (-> nav (.getBoundingClientRect) .-height)
        breakpoint (-> js/document (.querySelector ".sticky-nav-breakpoint"))]
    (-> js/document
        (rx/fromEvent "scroll")
        (.pipe
         (rx-op/map (fn [] (-> breakpoint (.getBoundingClientRect) .-top (#(if (neg? %) 1 0)))))
         (rx-op/distinctUntilChanged))
        (.subscribe (fn [v]
                      (let []
                        (do
                          (-> nav (.setAttribute "data-sticky" v))
                          (-> breakpoint (#(set! (-> % .-style .-height) (if (zero? v)
                                                                           0
                                                                           (str nav-height "px"))))))))))))
