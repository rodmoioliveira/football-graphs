(ns football.observables
  (:require
   [clojure.string :refer [includes? split trim replace lower-case]]
   ["rxjs" :as rx]
   ["rxjs/operators" :as rx-op]

   [football.matches :refer [labels-hash search-pool]]
   [football.store :refer [update-theme-store!]]
   [utils.dom :refer [dom
                      is-body-click?
                      match-item
                      reset-search-results!
                      get-metrics
                      get-current-theme
                      set-search-count!
                      reset-search-count!
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
        list$ (-> js/document

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

(defn search-bar$
  []
    (-> dom :search-bar
        (rx/fromEvent "input")
        (.pipe
         (rx-op/debounceTime 500)
         (rx-op/tap reset-search-results!)
         (rx-op/map (fn [e] (-> e .-target .-value trim lower-case))))
        (.subscribe
         (fn [v]
           (let [search-count (count v)]
             (cond
               (zero? search-count) (do
                                      (reset-search-results!)
                                      (reset-search-count!))
               (< 2 search-count) (->>
                                   search-pool
                                   (filter #(includes? (-> % :label lower-case) v))
                                   (map #(-> %
                                             :filename
                                             (split #"\.")
                                             first
                                             (replace #"," "")
                                             keyword
                                             labels-hash))
                                   (sort-by :label)
                                   (remove nil?)
                                   ((fn [matches]
                                      (set-search-count! matches)
                                      (if (zero? (count matches))
                                        (-> dom
                                            :search-result
                                            (.insertAdjacentHTML
                                              "beforeend"
                                              "<p>No results...</p>"))
                                        (doseq [match matches]
                                          (-> dom
                                              :search-result
                                              (.insertAdjacentHTML "beforeend" (match-item match))))))))

               :else (do
                       (reset-search-count!)
                       (-> dom
                           :search-result
                           (.insertAdjacentHTML
                            "beforeend"
                            "<p>Type a least 3 letters...</p>")))))))))
