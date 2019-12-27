; Inspiração:
; https://tsj101sports.com/2018/06/20/football-with-graph-theory/
(ns reactive.graph
  (:require
    [clojure.string :as str]
    [reactive.utils :refer [get-distance find-point radians-between]]
    ["d3" :as d3]))

(def canvas (-> js/document (.getElementById "canvas")))
(def ctx (-> canvas (.getContext "2d")))

; ==================================
; NODES
; ==================================
(def node-radius 35)
(def nodes-color "black")
(def nodes-outline {:color "white"
                    :width "1.5"})
(def node-font {:weight "700"
                :size "22px"
                :type "sans-serif"
                :color "white"
                :text-align "center"
                :base-line "middle"
                })
(def node-font-config
  (str/join " "
            [(node-font :weight)
             (node-font :size)
             (node-font :type)]))

; ==================================
; EDGES
; ==================================
; (def edges-alpha 0.5)
(def edges-padding 10)
(def dis-betw-edges (/ node-radius 3))
(def edges-color "grey")
(defn edge-width [value] (js/Math.sqrt value))
(def arrows {:edge-recoil 22
             :base-expansion 1.5
             :width 70})


; ==================================
; SIMULATIONS
; ==================================
(defn force-simulation
  [width height]
  (-> d3
      (.forceSimulation)
      (.force "center" (-> d3 (.forceCenter (/ width 2) (/ height 2))))
      (.force "link" (-> d3 (.forceLink) (.id (fn [d] (-> d .-id)))))
      (.force "change" (-> d3 (.forceManyBody)))))

(def simulation (force-simulation (.-width canvas) (.-height canvas)))

; ==================================
; DRAW FNS
; ==================================
(defn draw-edges
  [edge]
  (let [source-x (-> edge .-source .-initial_pos .-x)
        source-y (-> edge .-source .-initial_pos .-y)
        target-x (-> edge .-target .-initial_pos .-x)
        target-y (-> edge .-target .-initial_pos .-y)
        value (-> edge .-value)
        point-between (partial find-point source-x source-y target-x target-y)
        source-target-distance (get-distance
                                 source-x
                                 source-y
                                 target-x
                                 target-y)
        base-vector [source-target-distance 0]
        target-vector [(- target-x source-x) (- target-y source-y)]

        ; calculate angle of target projetion align with source along the x-axis
        radians (radians-between base-vector target-vector)
        orientation (cond
                      (and (< source-x target-x) (< source-y target-y)) radians
                      (and (> source-x target-x) (< source-y target-y)) radians
                      (and (= source-x target-x) (< source-y target-y)) radians
                      :else (- radians))]

    (doto ctx
      ; TODO: map weight to alpha on edge
      ; https://stackoverflow.com/questions/26584280/convert-given-range-values-to-domain-values-in-d3
      ; ((fn [v] (set! (.-globalAlpha v) edges-alpha)))
      ; translate to source node center point
      (.translate source-x source-y)
      ; rotate canvas by that angle
      (.rotate orientation)
      ; translate again between edges
      (.translate 0 dis-betw-edges)
      ; draw edges
      (.beginPath)
      (.moveTo (-> node-radius (+ edges-padding)) 0)
      (.lineTo (-> base-vector first (- node-radius edges-padding (arrows :edge-recoil))) (second base-vector))
      ((fn [v] (set! (.-lineWidth v) (edge-width value))))
      ((fn [v] (set! (.-strokeStyle v) edges-color)))
      (.stroke)

      ; draw arrows
      (.beginPath)
      ((fn [v] (set! (.-fillStyle v) edges-color)))
      (.moveTo (-> base-vector first (- node-radius edges-padding)) (-> base-vector second))
      (.lineTo (-> base-vector first (- (arrows :width))) (* (edge-width value) (arrows :base-expansion)))
      (.lineTo (-> base-vector first (- (arrows :width))) (- (* (edge-width value) (arrows :base-expansion))))
      (.fill)
      ; ; restore canvas
      (.setTransform))))

(defn draw-passes
  [edge]
  (-> ctx (.save))
  (draw-edges edge)
  (-> ctx (.restore)))

(defn draw-numbers
  [node]
  (let [x-initial-pos (-> node .-initial_pos .-x)
        y-initial-pos (-> node .-initial_pos .-y)]
    (doto ctx
      ((fn [v] (set! (.-font v) node-font-config)))
      ((fn [v] (set! (.-fillStyle v) (node-font :color))))
      ((fn [v] (set! (.-textAlign v) (node-font :text-align))))
      ((fn [v] (set! (.-textBaseline v) (node-font :base-line))))
      (.fillText (-> node .-id) x-initial-pos y-initial-pos))))

(defn draw-nodes
  [node]
  (let [x-initial-pos (-> node .-initial_pos .-x)
        y-initial-pos (-> node .-initial_pos .-y)]
    (doto ctx
      (.beginPath)
      (.moveTo (+ x-initial-pos node-radius) y-initial-pos)
      (.arc x-initial-pos y-initial-pos node-radius 0 (* 2 js/Math.PI))
      ((fn [v] (set! (.-fillStyle v) nodes-color)))
      (.fill)
      ((fn [v] (set! (.-strokeStyle v) (nodes-outline :color))))
      ((fn [v] (set! (.-lineWidth v) (nodes-outline :width))))
      (.stroke))))

(defn draw-players
  [node]
  (doto node
    (draw-nodes)
    (draw-numbers)))

(defn draw-graph
  [edges nodes]
  (doto ctx
    (.save)
    (.clearRect 0 0 (.-width canvas) (.-height canvas))
    ((fn [v] (set! (.-fillStyle v) "white")))
    (.fillRect 0 0 (.-width canvas) (.-height canvas)))
  (doseq [e edges] (draw-passes e))
  (doseq [n nodes] (draw-players n))
  (-> ctx (.restore)))

; ==================================
; FORCE GRAPF
; ==================================
(defn force-graph
  [data]
  (let [nodes (-> data .-nodes)
        edges (-> data .-links)]
    (-> simulation
        (.nodes nodes)
        (.on "tick" (fn [] (draw-graph edges nodes))))
    (-> simulation
        (.force "link")
        (.links edges))))

; ==================================
; MOCK DATA
; ==================================
(defn place-node
  [x-% y-%]
  #js {:x (* (.-width canvas) (/ x-% 100))  :y (* (.-height canvas) (/ y-% 100))})

(def mock-edges (for [source ["1" "5" "3" "11" "15" "16" "6" "8" "14" "6" "9" "7"]
                      target ["1" "5" "3" "11" "15" "16" "6" "8" "14" "6" "9" "7"]
                      :let [edge {:source source
                                  :target target
                                  :value (if (zero? (rand-int 2)) 1 50)}]
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
   ; :links (-> mock-edges vec)
   :links [
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
   })

; ==================================
; INIT FORCE GRAPF
; ==================================
(defn init-graph [] (-> mock-data clj->js force-graph))
