(ns football.config
  (:require
    [clojure.string :as str]
    ["d3" :as d3]))

; ==================================
; Themes
; ==================================
(def themes {:grey {:primary "grey"
                    :secondary "white"}
             :blue {:primary "blue"
                    :secondary "white"}
             :green {:primary "green"
                     :secondary "white"}
             :orange {:primary "orange"
                      :secondary "white"}
             :purple {:primary "purple"
                      :secondary "white"}
             :red {:primary "red"
                   :secondary "white"}})

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id theme]}]
  (let [mapping {:domains {:passes #js [0 100]}
                 :codomains {:edges-width #js [1 20]}}
        font {:weight "700"
              :size "35px"
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
                                 (.scaleLinear)
                                 (.domain (-> mapping :domains :passes))
                                 (.range #js [(-> theme :primary), "black"])
                                 (.interpolate (-> d3 (.-interpolateCubehelix) (.gamma 3))))
              :edges->width (-> d3
                                (.scalePow)
                                (.exponent 2)
                                (.domain (-> mapping :domains :passes))
                                (.range (-> mapping :codomains :edges-width)))}}))
