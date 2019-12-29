(ns reactive.config
  (:require
    [clojure.string :as str]
    ["d3" :as d3]))

; ==================================
; Themes
; ==================================
(def themes
  {
   :grey {:primary "black"
          :secondary "white"
          :edges (-> d3 (.-interpolateGreys))}
   :blue {:primary "black"
          :secondary "white"
          :edges (-> d3 (.-interpolateBlues))}
   :green {:primary "black"
           :secondary "white"
           :edges (-> d3 (.-interpolateGreens))}
   :orange {:primary "black"
            :secondary "white"
            :edges (-> d3 (.-interpolateOranges))}
   :purple {:primary "black"
            :secondary "white"
            :edges (-> d3 (.-interpolatePurples))}
   :red {:primary "black"
         :secondary "white"
         :edges (-> d3 (.-interpolateReds))}
   :viridis {:primary "black"
             :secondary "white"
             :edges (-> d3 (.-interpolateViridis))}
   :inferno {:primary "black"
             :secondary "white"
             :edges (-> d3 (.-interpolateInferno))}
   :magma {:primary "black"
           :secondary "white"
           :edges (-> d3 (.-interpolateMagma))}
   :plasma {:primary "black"
            :secondary "white"
            :edges (-> d3 (.-interpolatePlasma))}
   :cividis {:primary "black"
             :secondary "white"
             :edges (-> d3 (.-interpolateCividis))}
   :warm {:primary "black"
          :secondary "white"
          :edges (-> d3 (.-interpolateWarm))}
   :cool {:primary "black"
          :secondary "white"
          :edges (-> d3 (.-interpolateCool))}
   :turbo {:primary "black"
             :secondary "white"
             :edges (-> d3 (.-interpolateTurbo))}
   })

; ==================================
; Configuration hashmap
; ==================================
(defn config
  [{:keys [id theme]}]
  (let [mapping {:domains {:passes->color #js [(- 5) 130]
                           :passes->edge-width #js [0 100]}
                 :codomains {:passes<-edge-width #js [2 21]}}
        font {:weight "700"
              :size "22px"
              :type "sans-serif"
              :color (theme :secondary)
              :text-align "center"
              :base-line "middle"}
        node-radius 35]

    {:arrows {:recoil 22
              :expansion 0.9
              :width 75}
     :canvas (-> js/document (.getElementById id))
     :ctx (-> js/document (.getElementById id) (.getContext "2d"))
     :edges {:padding 10
             :distance-between (/ node-radius 2.2)}
     :nodes {:radius node-radius
             :fill {:color (theme :primary)}
             :outline {:color (theme :secondary)
                       :width "1.5"}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales {:edges->colors (-> d3
                                 (.scaleSequential (theme :edges))
                                 (.domain (-> mapping :domains :passes->color)))
              :edges->width (-> d3
                                (.scaleLinear)
                                (.domain (-> mapping :domains :passes->edge-width))
                                (.range (-> mapping :codomains :passes<-edge-width)))}}))

; ==================================
; Mock data
; ==================================
(defn place-node
  [x-% y-%]
  #js {:x (* (-> (config {:id "canvas0" :theme (-> themes :grey)}) :canvas .-width) (/ x-% 100))
       :y (* (-> (config {:id "canvas0" :theme (-> themes :grey)}) :canvas .-height) (/ y-% 100))})

(def mock-data
  {
   :nodes [
           {:id "7" :initial_pos (place-node 30 6)}
           {:id "9" :initial_pos (place-node 70 6)}
           {:id "14" :initial_pos (place-node 50 28)}
           {:id "8" :initial_pos (place-node 91 28)}
           {:id "6" :initial_pos (place-node 9 28)}
           {:id "16" :initial_pos (place-node 50 58)}
           {:id "15" :initial_pos (place-node 91 58)}
           {:id "11" :initial_pos (place-node 9 58)}
           {:id "3" :initial_pos (place-node 71 77)}
           {:id "5" :initial_pos (place-node 29 77)}
           {:id "1" :initial_pos (place-node 50 95)}
           ]
   :links (->
            [
             {:source "7" :target "14" :value 32}
             {:source "14" :target "6" :value 71}
             {:source "6" :target "14" :value 89}
             {:source "14" :target "8" :value 47}
             {:source "8" :target "14" :value 17}
             {:source "6" :target "1" :value 23}
             {:source "1" :target "6" :value 38}
             {:source "3" :target "1" :value 41}
             {:source "1" :target "3" :value 100}
             {:source "5" :target "1" :value 23}
             {:source "1" :target "5" :value 23}
             {:source "1" :target "7" :value 29}
             {:source "7" :target "1" :value 19}
             {:source "14" :target "16" :value 72}
             {:source "16" :target "14" :value 27}
             {:source "15" :target "16" :value 52}
             {:source "16" :target "15" :value 47}
             {:source "11" :target "16" :value 52}
             {:source "16" :target "11" :value 47}
             {:source "1" :target "16" :value 5}
             {:source "16" :target "1" :value 90}
             {:source "5" :target "11" :value 54}
             {:source "11" :target "5" :value 23}
             {:source "9" :target "11" :value 70}
             {:source "11" :target "9" :value 69}
             {:source "6" :target "11" :value 21}
             {:source "11" :target "6" :value 15}
             {:source "8" :target "11" :value 9}
             {:source "11" :target "8" :value 29}
             {:source "3" :target "11" :value 63}
             {:source "11" :target "3" :value 17}
             {:source "7" :target "11" :value 74}
             {:source "11" :target "7" :value 44}
             {:source "15" :target "11" :value 19}
             {:source "11" :target "15" :value 15}
             {:source "15" :target "8" :value 53}
             {:source "8" :target "15" :value 17}
             {:source "6" :target "7" :value 53}
             {:source "7" :target "6" :value 67}
             {:source "9" :target "8" :value 53}
             {:source "8" :target "9" :value 67}
             {:source "15" :target "5" :value 87}
             {:source "5" :target "15" :value 6}
             {:source "1" :target "9" :value 29}
             {:source "9" :target "1" :value 49}
             {:source "9" :target "7" :value 59}
             {:source "7" :target "9" :value 13}
             {:source "14" :target "7" :value 59}
             {:source "7" :target "14" :value 13}
             {:source "14" :target "9" :value 59}
             {:source "9" :target "14" :value 13}
             {:source "15" :target "3" :value 11}
             {:source "3" :target "15" :value 27}
             {:source "15" :target "14" :value 61}
             {:source "14" :target "15" :value 37}
             {:source "16" :target "8" :value 27}
             {:source "8" :target "16" :value 34}
             {:source "16" :target "6" :value 27}
             {:source "6" :target "16" :value 34}
             ]
            (#(sort-by :value %)))
   })

