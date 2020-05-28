(ns football.observables
  (:require
   ["rxjs" :as rx]
   ["rxjs/operators" :as rx-op]

   [football.store :refer [update-theme-store!]]
   [utils.dom :refer [dom
                      is-body-click?
                      get-metrics
                      get-current-theme
                      set-compare-text!
                      set-in-storage!
                      activate-nav
                      deactivate-nav]]
   [mapping.themes :refer [theme-identity
                           theme-reverse
                           get-theme-with]]))

(set! *warn-on-infer* true)

(defn select-metrics$
  []
  (let [display-passes (fn [{:keys [min-passes-to-display]}]
                         (set! (.-innerHTML (dom :min-passes-span)) (str "(" min-passes-to-display ")")))
        input$ (-> (rx/of
                    (-> dom :node-color-select)
                    (-> dom :node-area-select)
                    (-> dom :min-passes-input)
                    (-> dom :compare?))
                   (.pipe
                    (rx-op/mergeMap #(-> (rx/fromEvent % "input")
                                         (.pipe (rx-op/map
                                                 (fn [_]
                                                   (merge
                                                    (get-metrics)
                                                    (get-theme-with (partial theme-identity (get-current-theme)))))))))
                    (rx-op/tap (fn [obj]
                                 (set-compare-text! obj)
                                 (display-passes obj)
                                 (-> obj update-theme-store!)))))
        list$ (-> dom
                  :matches-lists
                  (rx/fromEvent "click")
                  (.pipe
                   (rx-op/filter (fn [e] (-> e .-target (.hasAttribute "data-match-id"))))
                   (rx-op/tap (fn [e] (-> e .-target (.setAttribute "data-visited" ""))))
                   (rx-op/map (fn [e] (-> e .-target (.getAttribute "data-match-id") keyword)))
                   (rx-op/map (fn [match-id]
                                (merge
                                 {:select-match match-id}
                                 (get-metrics)
                                 (get-theme-with (partial theme-identity (get-current-theme))))))
                   (rx-op/tap update-theme-store!)))
        toogle-theme$ (-> dom
                          :theme-btn
                          (rx/fromEvent "click")
                          (.pipe (rx-op/map (fn [_] (merge
                                                     (get-metrics)
                                                     (get-theme-with (partial theme-reverse (get-current-theme))))))
                                 (rx-op/tap (fn [obj]
                                              (-> obj update-theme-store!)
                                              (-> obj :theme (set-in-storage! "data-theme"))))))]
    {:input$ input$
     :toogle-theme$ toogle-theme$
     :list$ list$}))

(defn sticky-nav$
  []
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
      (.subscribe deactivate-nav)))

(def slider$
  (-> dom :slide-to-home
      (rx/fromEvent "click")))
