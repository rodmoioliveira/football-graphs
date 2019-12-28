(ns reactive.config
  (:require
    [clojure.string :as str]
    ["d3" :as d3]))

; ==================================
; Configuration hashmap
; ==================================
(defn config
  []
  (let [mapping {:domains {:passes->color #js [(- 50) 100]
                           :passes->edge-width #js [0 100]}
                 :codomains {:passes<-edge-width #js [2 11]}}
        font {:weight "700"
              :size "22px"
              :type "sans-serif"
              :color "white"
              :text-align "center"
              :base-line "middle"}
        node-radius 35]

    {:arrows {:recoil 22
              :expansion 1.5
              :width 70}
     :canvas (-> js/document (.getElementById "canvas"))
     :ctx (-> js/document (.getElementById "canvas") (.getContext "2d"))
     :edges {:padding 10
             :distance-between (/ node-radius 3)}
     :nodes {:radius node-radius
             :fill {:color "black"}
             :outline {:color "white"
                       :width "1.5"}
             :font (assoc font :full (str/join " " [(font :weight)
                                                    (font :size)
                                                    (font :type)]))}
     :scales {:edges->colors (-> d3
                                 (.scaleSequential (-> d3 (.-interpolateGreys)))
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
  #js {:x (* (-> (config) :canvas .-width) (/ x-% 100))
       :y (* (-> (config) :canvas .-height) (/ y-% 100))})

(def mock-edges (for [source ["1" "5" "3" "11" "15" "16" "6" "8" "14" "6" "9" "7"]
                      target ["1" "5" "3" "11" "15" "16" "6" "8" "14" "6" "9" "7"]
                      :let [edge {:source source
                                  :target target
                                  :value (if (zero? (rand-int 2)) 1 20)}]
                      :when (not= source target)]
                  edge))

(def mock-data
  {
   :nodes [
           {:id "7" :initial_pos (place-node 30 6)}
           {:id "9" :initial_pos (place-node 70 6)}
           {:id "6" :initial_pos (place-node 9 28)}
           {:id "14" :initial_pos (place-node 50 28)}
           {:id "8" :initial_pos (place-node 91 28)}
           {:id "6" :initial_pos (place-node 9 28)}
           {:id "16" :initial_pos (place-node 50 58)}
           {:id "15" :initial_pos (place-node 91 58)}
           {:id "11" :initial_pos (place-node 9 58)}
           {:id "3" :initial_pos (place-node 72 77)}
           {:id "5" :initial_pos (place-node 28 77)}
           {:id "1" :initial_pos (place-node 50 95)}
           ]
   ; :links (-> mock-edges (#(sort-by :value %)) vec)
   :links (->
            [
             {:source "6" :target "14" :value 1}
             {:source "14" :target "6" :value 100}
             {:source "8" :target "14" :value 1}
             {:source "14" :target "8" :value 100}
             {:source "6" :target "1" :value 1}
             {:source "1" :target "6" :value 100}
             {:source "3" :target "1" :value 1}
             {:source "1" :target "3" :value 100}
             {:source "5" :target "1" :value 23}
             {:source "1" :target "5" :value 2}
             {:source "1" :target "7" :value 2}
             {:source "7" :target "1" :value 2}
             {:source "1" :target "16" :value 2}
             {:source "16" :target "1" :value 2}
             {:source "5" :target "11" :value 2}
             {:source "11" :target "5" :value 48}
             {:source "9" :target "11" :value 48}
             {:source "11" :target "9" :value 48}
             {:source "6" :target "11" :value 15}
             {:source "11" :target "6" :value 15}
             {:source "8" :target "11" :value 48}
             {:source "11" :target "8" :value 48}
             {:source "3" :target "11" :value 48}
             {:source "11" :target "3" :value 48}
             {:source "7" :target "11" :value 48}
             {:source "11" :target "7" :value 2}
             {:source "15" :target "11" :value 2}
             {:source "11" :target "15" :value 2}
             {:source "15" :target "8" :value 67}
             {:source "8" :target "15" :value 17}
             {:source "15" :target "5" :value 67}
             {:source "5" :target "15" :value 67}
             {:source "1" :target "9" :value 2}
             {:source "9" :target "1" :value 2}
             {:source "14" :target "9" :value 2}
             {:source "9" :target "14" :value 2}
             {:source "15" :target "3" :value 2}
             {:source "3" :target "15" :value 2}
             {:source "15" :target "14" :value 2}
             {:source "14" :target "15" :value 2}
             {:source "16" :target "6" :value 2}
             {:source "6" :target "16" :value 2}
             ]
            (#(sort-by :value %)))
   })

