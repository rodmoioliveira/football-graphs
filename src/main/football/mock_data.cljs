(ns football.mock-data
  (:require
   [football.utils :refer [assoc-pos]]
   [football.tatical :refer [tatical-schemes]]))

(def team-formations
  {:4-2-3-1 [{:id "7" :pos :ATC}
             {:id "9" :pos :VOC}
             {:id "8" :pos :PTD}
             {:id "6" :pos :PTE}
             {:id "16" :pos :VOD}
             {:id "14" :pos :VOE}
             {:id "15" :pos :LAD}
             {:id "11" :pos :LAE}
             {:id "3" :pos :ZAD}
             {:id "5" :pos :ZAE}
             {:id "1" :pos :GOL}]
   :4-3-3 [{:id "7" :pos :ATC}
           {:id "9" :pos :VOC}
           {:id "8" :pos :PTD}
           {:id "6" :pos :PTE}
           {:id "16" :pos :VOD}
           {:id "14" :pos :VOE}
           {:id "15" :pos :LAD}
           {:id "11" :pos :LAE}
           {:id "3" :pos :ZAD}
           {:id "5" :pos :ZAE}
           {:id "1" :pos :GOL}]})

; ==================================
; Mock data
; ==================================
(defn mock-data
  [canvas formation]
  {:nodes (assoc-pos
           canvas
           (-> team-formations formation)
           formation
           tatical-schemes)
   :links (->
           [{:source "7" :target "14" :value 32}
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
            {:source "16" :target "6" :value 1}
            {:source "6" :target "16" :value 1}]
           (#(sort-by :value %)))})
