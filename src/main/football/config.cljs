(ns football.config
  (:require
   [clojure.string :as str]
   ["d3" :as d3]))

; ==================================
; Themes
; ==================================
(def themes {:grey {:primary "grey"
                    :secondary "white"
                    :accent "#dedcdb"}
             :blue {:primary "blue"
                    :secondary "white"
                    :accent "#c5c2ff"}
             :orange {:primary "orange"
                      :secondary "white"
                      :accent "#ffecc2"}
             :purple {:primary "purple"
                      :secondary "white"
                      :accent "#ffb8f6"}
             :red {:primary "red"
                   :secondary "white"
                   :accent "#ffa6a3"}
             ; Costa Rica
             :16817 {:primary "#002780"
                     :secondary "white"
                     :accent "#adb5ff"}
             ; Switzerland
             :6697 {:primary "#d52b1e"
                    :secondary "white"
                    :accent "#ffcac2"}
             ; Serbia
             :17322 {:primary "#c73339"
                     :secondary "white"
                     :accent "#ffcac2"}
             ; Brazil
             :6380 {:primary "#00912F"
                    :secondary "white"
                    :accent "#03f081"}})

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id theme max-passes radius-metric]}]
  (let [mapping {:domains {:passes #js [1 max-passes]
                           :degree #js [1 133]
                           :local-clustering-coefficient #js [1 0.7]
                           :betweenness-centrality #js [0 0.2]
                           :closeness-centrality #js [0 0.6]
                           :in-degree #js [1 70]
                           :out-degree #js [1 70]}
                 :codomains {:edges-width #js [1 20]
                             :radius #js [20 50]}}
        font {:weight "700"
              :size "25px"
              :type "'Open sans', sans-serif"
              :color "black"
              :text-align "center"
              :base-line "middle"}
        canvas (-> js/document (.getElementById id))
        radius-scale #(-> d3
                          ; FIXME: change scale to area...
                          (.scalePow)
                          (.exponent 1.5)
                          (.domain (-> mapping :domains %))
                          (.range (-> mapping :codomains :radius)))
        degree (radius-scale :degree)
        in-degree (radius-scale :in-degree)
        out-degree (radius-scale :out-degree)
        betweenness-centrality (radius-scale :betweenness-centrality)
        closeness-centrality (radius-scale :closeness-centrality)
        local-clustering-coefficient (radius-scale :local-clustering-coefficient)
        edges->colors (-> d3
                          (.scalePow)
                          (.exponent 0.1)
                          (.domain (-> mapping :domains :passes))
                          (.range #js [(-> theme :accent), "black"])
                          (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3))))
        edges->width (-> d3
                         (.scalePow)
                         (.exponent 0.9)
                         (.domain (-> mapping :domains :passes))
                         (.range (-> mapping :codomains :edges-width)))
        scales {:degree degree
                :in-degree in-degree
                :out-degree out-degree
                :betweenness-centrality betweenness-centrality
                :closeness-centrality closeness-centrality
                :local-clustering-coefficient local-clustering-coefficient
                :edges->colors edges->colors
                :edges->width edges->width}]

    {:arrows {:recoil 19
              :expansion 1.2
              :width 30}
     :canvas canvas
     :ctx (-> canvas (.getContext "2d"))
     :edges {:padding 10
             :distance-between 5
             :alpha 0.03}
     :nodes {:radius-metric radius-metric
             :radius-click 5
             :fill {:color (theme :accent)}
             :active {:color "#ebd1fe"
                      :outline "purple"}
             :name-position 0
             :outline {:color (theme :primary)
                       :width 1.5}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales scales}))
