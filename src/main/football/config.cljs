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
                     :accent "#03f081"}
             })

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id theme]}]
  (let [mapping {:domains {:passes #js [1 31]}
                 :codomains {:edges-width #js [2 22]}}
        font {:weight "700"
              :size "25px"
              :type "'Open sans', sans-serif"
              :color "black"
              :text-align "center"
              :base-line "middle"}
        node-radius 20
        canvas (-> js/document (.getElementById id))]

    {:arrows {:recoil 19.5
              :expansion 0.9
              :width 80}
     :canvas canvas
     :ctx (-> canvas (.getContext "2d"))
     :edges {:padding 10
             :distance-between (/ node-radius 2.5)}
     :nodes {:radius node-radius
             :fill {:color (theme :primary)}
             :active {:color (theme :accent)}
             :name-position (+ node-radius 15)
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
