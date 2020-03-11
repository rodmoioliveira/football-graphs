(ns football.metrics-nav
  (:require ["rxjs" :as rx]
            ["rxjs/operators" :as rx-op]))

(set! *warn-on-infer* true)

(def dom
  {:node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
   :node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
   :coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
   :position-select (-> js/document (.querySelector (str "[data-metric='position']")))
   :min-passes-input (-> js/document (.querySelector (str "[data-metric='min-passes-to-display']")))
   :min-passes-span (-> js/document (.querySelector (str "[data-min-passes-value]")))})

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

(def theme-mapping
  {:light {:theme "dark"
           :text "Light Mode"
           :background "#121010"
           :font-color "white"}
   :dark {:theme "light"
          :text "Dark Mode"
          :background "white"
          :font-color "black"}})

(defn sticky-nav$
  []
  (let [menu (-> js/document (.querySelector ".nav-menu"))
        theme-btn (-> js/document (.querySelector "[data-toogle-theme]"))
        body-theme (-> js/document (.querySelector "[data-theme]"))
        current-theme (fn [] (-> body-theme (.getAttribute "data-theme") keyword))
        activate-btn (-> js/document (.querySelector "[data-active-metrics]"))
        deactivate-btn (-> js/document (.querySelector "[data-deactivate-metrics]"))
        nav (-> js/document (.querySelector ".nav-metrics"))
        breakpoint (-> js/document (.querySelector ".sticky-nav-breakpoint"))]

    (-> js/document
        (rx/fromEvent "scroll")
        (.pipe
         (rx-op/map (fn [] (-> breakpoint (.getBoundingClientRect) .-top (#(if (neg? %) 1 0)))))
         (rx-op/distinctUntilChanged))
        (.subscribe (fn [v]
                      (do
                        (-> menu (.setAttribute "data-sticky" v))))))

    (-> theme-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_] (let [mapped-theme (-> (current-theme) theme-mapping :theme)
                                  mapped-text (-> (current-theme) theme-mapping :text)]
                              (do
                              (-> theme-btn (#(set! (.-innerHTML %) mapped-text)))
                              (-> body-theme
                                  (.setAttribute "data-theme" mapped-theme)))))))

    (-> activate-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_] (-> nav (.setAttribute "data-active" 1)))))

    (-> deactivate-btn
        (rx/fromEvent "click")
        (.subscribe (fn [_] (-> nav (.setAttribute "data-active" 0)))))))
