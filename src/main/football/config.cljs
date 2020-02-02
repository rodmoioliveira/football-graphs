(ns football.config
  (:require
   [clojure.string :as str]
   ["d3" :as d3]))

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id node-radius-metric node-color-metric meta-data name-position]}]
  ; (-> meta-data node-radius-metric (#((juxt :min :max) %)) print)
  (let [get-ranges (fn [metric] (-> meta-data metric (#((juxt :min :max) %))))

        ; ==================================
        ; Domains and Codomains
        ; ==================================
        mapping {:domains
                 {:passes (-> (get-ranges :passes) clj->js)
                  :degree (-> (get-ranges :degree) clj->js)
                  :in-degree (-> (get-ranges :in-degree) clj->js)
                  :out-degree (-> (get-ranges :out-degree) clj->js)
                  :katz-centrality (-> (get-ranges :katz-centrality) clj->js)
                  :betweenness-centrality (-> (get-ranges :betweenness-centrality) clj->js)
                  :local-clustering-coefficient (-> (get-ranges :local-clustering-coefficient) reverse clj->js)
                  :closeness-centrality (-> (get-ranges :closeness-centrality) clj->js)
                  :alpha-centrality (-> (get-ranges :alpha-centrality) clj->js)
                  :eigenvector-centrality (-> (get-ranges :eigenvector-centrality) clj->js)}
                 :codomains {:edges-width #js [2 25]
                             :radius #js [20 50]
                             :colors {:cold #js ["#bbdefb", "#0d47a1"]
                                      :hot #js ["#ffff00", "#ff0000"]}}}

        ; ==================================
        ; Font
        ; ==================================
        font {:weight "700"
              :size "25px"
              :type "'Open sans', sans-serif"
              :color "black"
              :text-align "center"
              :base-line "middle"}

        ; ==================================
        ; Canvas
        ; ==================================
        canvas (-> js/document (.getElementById id))

        ; ==================================
        ; Scales
        ; ==================================
        edges->colors (-> d3
                          (.scalePow)
                          (.exponent 0.1)
                          (.domain (-> mapping :domains :passes))
                          (.range (-> mapping :codomains :colors :cold))
                          (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3))))
        edges->width (-> d3
                         (.scaleLinear)
                         (.domain (-> mapping :domains :passes))
                         (.range (-> mapping :codomains :edges-width)))
        node-color-scale #(-> d3
                              (.scalePow)
                              (.exponent 1)
                              (.domain (-> mapping :domains %))
                              (.range (-> mapping :codomains :colors :hot))
                              (.interpolate (-> d3 (.-interpolateRgb) (.gamma 3))))
        node-radius-scale #(-> d3
                               ; https://bl.ocks.org/d3indepth/775cf431e64b6718481c06fc45dc34f9
                               (.scaleSqrt)
                               (.domain (-> mapping :domains %))
                               (.range (-> mapping :codomains :radius)))
        map-scale {:radius node-radius-scale
                   :color node-color-scale}
        degree #((-> map-scale %) :degree)
        in-degree #((-> map-scale %) :in-degree)
        out-degree #((-> map-scale %) :out-degree)
        betweenness-centrality #((-> map-scale %) :betweenness-centrality)
        closeness-centrality #((-> map-scale %) :closeness-centrality)
        local-clustering-coefficient #((-> map-scale %) :local-clustering-coefficient)
        alpha-centrality #((-> map-scale %) :alpha-centrality)
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
                :edges->colors edges->colors
                :edges->width edges->width}]

    ; ==================================
    ; Config Object
    ; ==================================
    {:arrows {:recoil 19
              :expansion 1.2
              :width 30}
     :canvas canvas
     :ctx (-> canvas (.getContext "2d"))
     :edges {:padding 10
             :distance-between 5
             :alpha 0.03}
     :nodes {:node-radius-metric node-radius-metric
             :node-color-metric node-color-metric
             :radius-click 5
             :active {:color "#ebd1fe"
                      :outline "#999"}
             :name-position (or name-position :top)
             :outline {:color "#999"
                       :width 1.5}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales scales}))
