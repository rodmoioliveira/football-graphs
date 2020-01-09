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
             :green {:primary "green"
                     :secondary "white"
                     :accent "#a2dea6"}
             :orange {:primary "orange"
                      :secondary "white"
                      :accent "#ffecc2"}
             :purple {:primary "purple"
                      :secondary "white"
                      :accent "#ffb8f6"}
             :red {:primary "red"
                   :secondary "white"
                   :accent "#ffa6a3"}})

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id theme]}]
  (let [mapping {:domains {:passes #js [1 26]}
                 :codomains {:edges-width #js [1 30]}}
        font {:weight "700"
              :size "25px"
              :type "sans-serif"
              :color (theme :secondary)
              :text-align "center"
              :base-line "middle"}
        node-radius 45
        canvas (-> js/document (.getElementById id))]

    {:arrows {:recoil 19.5
              :expansion 0.9
              :width 75}
     :canvas canvas
     :ctx (-> canvas (.getContext "2d"))
     :edges {:padding 10
             :distance-between (/ node-radius 2.2)}
     :nodes {:radius node-radius
             :fill {:color "black"}
             :active {:color (theme :primary)}
             :outline {:color (theme :secondary)
                       :width "1.5"}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales {:edges->colors (-> d3
                                 (.scalePow)
                                 (.exponent 0.1)
                                 (.domain (-> mapping :domains :passes))
                                 (.range #js [(-> theme :accent), "black"])
                                 (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3))))
              :edges->width (-> d3
                                (.scalePow)
                                (.exponent 0.9)
                                (.domain (-> mapping :domains :passes))
                                (.range (-> mapping :codomains :edges-width)))}}))
