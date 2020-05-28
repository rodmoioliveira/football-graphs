(ns football.config
  (:require
   ["d3" :as d3]
   [clojure.string :as str]))

(defn config
  [{:keys [id
           node-radius-metric
           node-color-metric
           global-stats
           team-stats
           name-position
           font-color
           mobile?
           node-color-range
           edge-color-range
           outline-node-color]}]
  (let [get-ranges (fn [metric global-stats?]
                     (-> (if global-stats? global-stats team-stats) metric (#((juxt :min :max) %))))
        mapping {:domains
                 {:passes
                  (fn
                    [global-stats?]
                    (-> (get-ranges :passes global-stats?)
                        clj->js))
                  :degree
                  (fn
                    [global-stats?]
                    (-> (get-ranges :degree global-stats?)
                        clj->js))
                  :betweenness-centrality
                  (fn
                    [global-stats?]
                    (-> (get-ranges :betweenness-centrality global-stats?)
                        clj->js))
                  :local-clustering-coefficient
                  (fn
                    [global-stats?]
                    (-> (get-ranges :local-clustering-coefficient global-stats?)
                        reverse
                        clj->js))
                  :closeness-centrality
                  (fn
                    [global-stats?]
                    (-> (get-ranges :closeness-centrality global-stats?) clj->js))
                  ; :in-degree (-> (get-ranges :in-degree) clj->js)
                  ; :out-degree (-> (get-ranges :out-degree) clj->js)
                  ; :katz-centrality (-> (get-ranges :katz-centrality) clj->js)
                  ; :current_flow_betweenness_centrality (-> (get-ranges :current_flow_betweenness_centrality) clj->js)
                  ; :alpha-centrality (-> (get-ranges :alpha-centrality) clj->js)
                  ; :eigenvector-centrality (-> (get-ranges :eigenvector-centrality) clj->js)
                  }
                 :codomains {:edges-width #js [1 10]
                             :radius #js [8 23]
                             :colors {:edges edge-color-range
                                      :nodes node-color-range}}}
        font {:weight "400"
              :size "22px"
              :type "'Alegreya', serif"
              :color (or font-color "black")
              :text-align "center"
              :base-line "middle"}
        canvas (-> js/document (.getElementById id))
        edges->colors (fn [global-stats? range]
                        (-> d3
                            (.scalePow)
                            (.exponent 1)
                            (.domain ((-> mapping :domains :passes) global-stats?))
                            (.range range)
                            (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3)))))
        edges->width (fn [global-stats?]
                       (-> d3
                           (.scaleLinear)
                           (.domain ((-> mapping :domains :passes) global-stats?))
                           (.range (-> mapping :codomains :edges-width))))
        node-color-scale (fn [domain]
                           (fn [codomain global-stats?]
                             (-> d3
                                 (.scalePow)
                                 (.exponent 1)
                                 (.domain ((-> mapping :domains domain) global-stats?))
                                 (.range codomain)
                                 (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3))))))
        node-radius-scale (fn [domain]
                            (fn [global-stats?]
                              (-> d3
                               ; https://bl.ocks.org/d3indepth/775cf431e64b6718481c06fc45dc34f9
                                  (.scaleSqrt)
                                  (.domain ((-> mapping :domains domain) global-stats?))
                                  (.range (-> mapping :codomains :radius)))))
        map-scale {:radius node-radius-scale
                   :color node-color-scale}
        degree #((-> map-scale %) :degree)
        in-degree #((-> map-scale %) :in-degree)
        out-degree #((-> map-scale %) :out-degree)
        betweenness-centrality #((-> map-scale %) :betweenness-centrality)
        closeness-centrality #((-> map-scale %) :closeness-centrality)
        local-clustering-coefficient #((-> map-scale %) :local-clustering-coefficient)
        alpha-centrality #((-> map-scale %) :alpha-centrality)
        current_flow_betweenness_centrality #((-> map-scale %) :current_flow_betweenness_centrality)
        katz-centrality #((-> map-scale %) :katz-centrality)
        eigenvector-centrality #((-> map-scale %) :eigenvector-centrality)
        scales {:degree degree
                :in-degree in-degree
                :out-degree out-degree
                :betweenness-centrality betweenness-centrality
                :closeness-centrality closeness-centrality
                :katz-centrality katz-centrality
                :local-clustering-coefficient local-clustering-coefficient
                :alpha-centrality alpha-centrality
                :eigenvector-centrality eigenvector-centrality
                :current_flow_betweenness_centrality current_flow_betweenness_centrality
                :edges->colors edges->colors
                :edges->width edges->width}]
    {:arrows {:recoil 12
              :expansion 1.5
              :width 34}
     :canvas canvas
     :ctx (-> canvas (.getContext "2d"))
     :edges {:padding 15
             :distance-between 8
             :alpha 0}
     :nodes {:node-radius-metric node-radius-metric
             :node-color-metric node-color-metric
             :radius-click (if mobile? 10 0.5)
             :active {:color "#ebd1fe"
                      :outline "#9811fa"}
             :name-position (or name-position :top)
             :outline {:color outline-node-color
                       :width 1.5}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales scales}))
