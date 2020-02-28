(ns football.draw-graph
  (:require
   [clojure.string :refer [split]]
   [utils.core :refer [get-distance radians-between find-node]]
   ["d3" :as d3]))

(set! *warn-on-infer* true)

(defn draw-edges
  "Draw the edges of passes between players."
  [{:keys [^js edge config active-node nodeshash]}]
  (let [source-x (-> edge .-source .-coord .-x)
        source-y (-> edge .-source .-coord .-y)
        target-x (-> edge .-target .-coord .-x)
        target-y (-> edge .-target .-coord .-y)
        value (-> edge .-value)
        source-target-distance (get-distance
                                source-x
                                source-y
                                target-x
                                target-y)
        base-vector [source-target-distance 0]
        target-vector [(- target-x source-x) (- target-y source-y)]

        ; calculate angle of target projetion align with source along the x-axis
        radians (radians-between base-vector target-vector)
        orientation (cond (> source-y target-y) (- radians) :else radians)

        ; set alpha value for active edges
        active-edges (= (-> edge .-source .-id) (-> (or active-node #js {:id nil}) .-id))
        alpha-value (if active-node (if active-edges 1 (-> config :edges :alpha)) 1)

        ; get metric name, radius scale and values
        node-radius-metric-name (-> config :nodes :node-radius-metric name)
        radius-scale (-> config :scales (#(get-in % [(-> config :nodes :node-radius-metric)])) (#(% :radius)))
        source-radius (-> edge
                          .-source
                          .-id
                          (#(aget nodeshash %))
                          ((fn [^js v] (-> v .-metrics)))
                          (#(aget % node-radius-metric-name))
                          radius-scale)
        target-radius (-> edge
                          .-target
                          .-id
                          (#(aget nodeshash %))
                          ((fn [^js v] (-> v .-metrics)))
                          (#(aget % node-radius-metric-name))
                          radius-scale)]

    (doto (-> config :ctx)
      (.save)
      ; translate to source node center point
      (.translate source-x source-y)
      ; rotate canvas
      (.rotate orientation)
      ; translate again between edges
      (.translate 0 (-> config :edges :distance-between))
      ; draw edges
      (.beginPath)
      (.moveTo (-> source-radius (+ (-> config :edges :padding))) 0)
      (.lineTo
       (-> base-vector
           first
           (- target-radius
              (-> config :edges :padding)
              (-> config :arrows :recoil)))
       (second base-vector))
      ((fn [v] (set! (.-lineWidth v) ((-> config :scales :edges->width) value))))
      ((fn [v] (set! (.-globalAlpha v) alpha-value)))
      ((fn [v] (set! (.-strokeStyle v) ((-> config :scales :edges->colors) value))))
      (.stroke)

      ; draw arrows
      (.beginPath)
      ((fn [v] (set! (.-fillStyle v) ((-> config :scales :edges->colors) value))))
      (.moveTo
       (-> base-vector first (- target-radius (-> config :edges :padding)))
       (-> base-vector second))
      (.lineTo
       (-> base-vector first (- target-radius (-> config :arrows :width)))
       (* ((-> config :scales :edges->width) value) (-> config :arrows :expansion)))
      (.lineTo
       (-> base-vector first (- target-radius (-> config :arrows :width)))
       (- (* ((-> config :scales :edges->width) value) (-> config :arrows :expansion))))
      (.fill)

      ; restore canvas
      (.setTransform)
      (.restore))))

(defn draw-players-names
  "Draw the players names."
  [{:keys [node config]}]
  (let [x-pos (-> node .-coord .-x)
        y-pos (-> node .-coord .-y)

        ; Metrics for sizing node
        node-radius-metric-name (-> config :nodes :node-radius-metric name)
        node-radius-metric-value (-> node .-metrics (#(aget % node-radius-metric-name)))
        radius-scale (-> config
                         :scales
                         (#(get-in % [(-> config :nodes :node-radius-metric)]))
                         (#(% :radius)))
        radius (radius-scale node-radius-metric-value)

        ; Player name position
        name-position (-> config :nodes :name-position)
        positions {:center y-pos
                   :top (- y-pos 20 radius)
                   :bottom (+ y-pos 20 radius)}]
    (doto (-> config :ctx)
      ((fn [v] (set! (.-font v) (-> config :nodes :font :full))))
      ((fn [v] (set! (.-fillStyle v) (-> config :nodes :font :color))))
      ((fn [v] (set! (.-textAlign v) (-> config :nodes :font :text-align))))
      ((fn [v] (set! (.-textBaseline v) (-> config :nodes :font :base-line))))
      (.fillText (-> node
                     (aget "short-name")
                     reverse
                     first
                     (split #" ")
                     ((fn [s] (cond
                                (> (count s) 2) (get s 2)
                                (> (count s) 1) (second s)
                                :else (first s)))))
                 x-pos (-> positions name-position)))))

(defn draw-nodes
  "Draw the players nodes."
  [{:keys [node config]}]
  (let [x-pos (-> node .-coord .-x)
        y-pos (-> node .-coord .-y)
        is-active? (-> node .-active)
        active-color #(if is-active? (-> config :nodes :active :color) %)
        active-outline #(if is-active? (-> config :nodes :active :outline) %)

        ; Metrics for coloring node
        node-color-metric-name (-> config :nodes :node-color-metric name)
        node-color-metric-value (-> node .-metrics (#(aget % node-color-metric-name)))
        color-scale (-> config
                        :scales
                        (#(get-in % [(-> config :nodes :node-color-metric)]))
                        (#(% :color)))
        color (color-scale node-color-metric-value)

        ; Metrics for sizing node
        node-radius-metric-name (-> config :nodes :node-radius-metric name)
        node-radius-metric-value (-> node .-metrics (#(aget % node-radius-metric-name)))
        radius-scale (-> config
                         :scales
                         (#(get-in % [(-> config :nodes :node-radius-metric)]))
                         (#(% :radius)))
        radius (radius-scale node-radius-metric-value)]
    (doto (-> config :ctx)
      (.beginPath)
      (.moveTo (+ x-pos radius) y-pos)
      (.arc x-pos y-pos radius 0 (* 2 js/Math.PI))
      ((fn [v] (set! (.-fillStyle v) (-> color active-color))))
      (.fill)
      ((fn [v] (set! (.-strokeStyle v) (-> config :nodes :outline :color active-outline))))
      ((fn [v] (set! (.-lineWidth v) (-> config :nodes :outline :width))))
      (.stroke))))

(defn draw-players
  "Draw nodes and players names."
  [obj]
  (doto obj
    (draw-nodes)
    (draw-players-names)))

(defn draw-graph
  "Draw all graph elements."
  [{:keys [edges nodes config nodeshash active-node]}]
  (doseq [e edges] (draw-edges {:edge e
                                :nodeshash nodeshash
                                :config config
                                :active-node active-node}))
  (doseq [n nodes] (draw-players {:node n
                                  :config config})))

(defn draw-background
  "Draw the field background."
  [^js config ^js data]
  (let [ctx (-> config :ctx)
        background-color (-> data .-field .-background)]
    (doto ctx
      (.clearRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height))
      ((fn [v] (set! (.-fillStyle v) background-color)))
      (.fillRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height)))))

(defn draw-borders
  "Draw field borders."
  [ctx padding width length]
  (doto ctx
    (.beginPath)
    (.moveTo padding padding)
    (.lineTo padding (- width padding))
    (.lineTo (- length padding) (- width padding))
    (.lineTo (- length padding) padding)
    (.lineTo padding padding)
    (.stroke)))

(defn draw-corners
  "Draw field corners."
  [ctx padding width length corner-radius]
  (doto ctx
    (.beginPath)
    (.arc padding padding corner-radius 0 (/ js/Math.PI 2))
    (.stroke)
    (.beginPath)
    (.arc padding (- width padding) corner-radius (* js/Math.PI 1.5) (* 2 js/Math.PI))
    (.stroke)
    (.beginPath)
    (.arc (- length padding) (- width padding) corner-radius (* 1 js/Math.PI) (* 1.5 js/Math.PI))
    (.stroke)
    (.beginPath)
    (.arc (- length padding) padding corner-radius (* 0.5 js/Math.PI) (* 1 js/Math.PI))
    (.stroke)))

(defn draw-midfield-line
  "Draw midfield line."
  [ctx padding width length flip?]
  (doto ctx
    (.beginPath)
    (#(if flip?
        (doto %
          (.moveTo padding (/ width 2))
          (.lineTo (- length padding) (/ width 2)))
        (doto %
          (.moveTo (/ length 2) padding)
          (.lineTo (/ length 2) (- width padding)))))
    (.stroke)))

(defn draw-midfield-circle
  "Draw midfield circle."
  [ctx width length flip? midfield-cicle-radius]
  (doto ctx
    (.beginPath)
    (.arc
     (/ length 2)
     (/ width 2)
     (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
     0
     (* 2 js/Math.PI))
    (.stroke)))

(defn draw-midfield-point
  "Draw midfield point."
  [ctx width length midfield-point-radius]
  (doto ctx
    (.beginPath)
    (.arc (/ length 2) (/ width 2) midfield-point-radius 0 (* 2 js/Math.PI))
    (.fill)))

(defn draw-goals-rects
  "Draw all gol rect areas."
  [ctx
   {:keys [flip?
           length
           width
           padding
           gol-length
           gol-area-length
           penal-area-length
           penal-area-width
           gol-area-width]}]
  (if flip?
    (doto ctx
      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect (- (/ length 2) (/ width (* 2 gol-length))) 0 (/ width gol-length) padding)
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 gol-area-length)))
       padding
       (/ width gol-area-length)
       (/ width gol-area-width))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 penal-area-length)))
       padding
       (/ width penal-area-length)
       (/ width penal-area-width))
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect (- (/ length 2) (/ width (* 2 gol-length))) (- width padding) (/ width gol-length) padding)
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 gol-area-length)))
       (- width padding (/ width gol-area-width))
       (/ width gol-area-length)
       (/ width gol-area-width))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 penal-area-length)))
       (- width padding (/ width penal-area-width))
       (/ width penal-area-length)
       (/ width penal-area-width))
      (.stroke))

    (doto ctx
      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect
       0
       (- (/ width 2) (/ length (* 2 gol-length)))
       padding
       (/ length gol-length))
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       padding
       (- (/ width 2) (/ length (* 2 gol-area-length)))
       (/ length gol-area-width)
       (/ length gol-area-length))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       padding
       (- (/ width 2) (/ length (* 2 penal-area-length)))
       (/ length penal-area-width)
       (/ length penal-area-length))
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect
       (- length padding)
       (- (/ width 2) (/ length (* 2 gol-length)))
       padding
       (/ length gol-length))
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- length padding (/ length gol-area-width))
       (- (/ width 2) (/ length (* 2 gol-area-length)))
       (/ length gol-area-width)
       (/ length gol-area-length))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- length padding (/ length penal-area-width))
       (- (/ width 2) (/ length (* 2 penal-area-length)))
       (/ length penal-area-width)
       (/ length penal-area-length))
      (.stroke))))

(defn draw-penal-arcs
  "Draw all penal arcs."
  [ctx
   {:keys [flip?
           length
           width
           midfield-cicle-radius
           midfield-point-radius]}]
  (if flip?
    (doto ctx
      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 8.3)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       (* 0.2 js/Math.PI)
       (* 0.8 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 8.3)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 1.136)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       (* 1.205 js/Math.PI)
       (/ js/Math.PI 0.557))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 1.136)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))

    (doto ctx
      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 8.3)
       (/ width 2)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       (* 1.701 js/Math.PI)
       (* 2.299 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 8.3)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 1.136)
       (/ width 2)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       (* 0.708 js/Math.PI)
       (* 1.296 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 1.136)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))))

(defn draw-field
  "Draw soccer field on canvas."
  [dimensions ^js data config]
  (let [[a b] (sort dimensions)
        flip? (-> data .-orientation (#(or (= % "gol-bottom") (= % "gol-top"))))
        [width length] (if flip? [b a] [a b])
        field-data (-> data .-field)
        corner-radius (if flip? (/ width 100) (/ length 100))
        padding 10
        gol-length 16
        gol-area-length 5.7
        midfield-cicle-radius 11.3
        midfield-point-radius 3
        gol-area-width 18
        penal-area-width 6.1
        penal-area-length 2.52]
    (doto (-> config :ctx)
      ((fn [v] (set! (.-strokeStyle v) (aget field-data "lines-color"))))
      ((fn [v] (set! (.-fillStyle v) (aget field-data "lines-color"))))
      ((fn [v] (set! (.-lineWidth v) (aget field-data "lines-width"))))

      (draw-borders padding width length)
      (draw-corners padding width length corner-radius)
      (draw-midfield-line padding width length flip?)
      (draw-goals-rects
       {:flip? flip?
        :length length
        :width width
        :padding padding
        :gol-length gol-length
        :gol-area-length gol-area-length
        :penal-area-length penal-area-length
        :penal-area-width penal-area-width
        :gol-area-width gol-area-width})
      (draw-penal-arcs
       {:flip? flip?
        :length length
        :width width
        :midfield-cicle-radius midfield-cicle-radius
        :midfield-point-radius midfield-point-radius})
      (draw-midfield-circle width length flip? midfield-cicle-radius)
      (draw-midfield-point width length midfield-point-radius))))

(defn on-node-click
  "On node click, only display that player passes network."
  [{:keys [edges nodes config nodeshash data]}]
  (let [canvas-current-dimensions (-> config :canvas (.getBoundingClientRect))
        x-domain #js [0 (-> canvas-current-dimensions .-width)]
        y-domain #js [0 (-> canvas-current-dimensions .-height)]
        x-codomain #js [0 (-> config :canvas .-width)]
        y-codomain #js [0 (-> config :canvas .-height)]
        mapping-x (-> d3
                      (.scaleLinear)
                      (.domain x-domain)
                      (.range x-codomain))
        mapping-y (-> d3
                      (.scaleLinear)
                      (.domain y-domain)
                      (.range y-codomain))
        x (or (-> d3 .-event .-layerX) (-> d3 .-event .-offsetX))
        y (or (-> d3 .-event .-layerY) (-> d3 .-event .-offsetY))
        node (find-node
              config
              (-> canvas-current-dimensions .-width)
              nodes
              (mapping-x x)
              (mapping-y y))]

    (doseq [n nodes] (set! (.-active n) false))
    (when node (set! (.-active node) (not (-> node .-active))))

    (draw-background config data)
    (-> data (aget "canvas-dimensions") (draw-field data config))
    (draw-graph {:edges edges
                 :config config
                 :nodes nodes
                 :nodeshash nodeshash
                 :active-node node})))

(defn force-graph
  "Draw force graph elements."
  [{:keys [^js data config]}]
  (let [nodes (-> data .-nodes)
        nodeshash (-> data ^:export .-nodeshash)
        edges (-> data .-links)
        simulation (-> d3
                       (.forceSimulation)
                       (.force "link" (-> d3
                                          (.forceLink)
                                          (.id (fn [d] (-> d .-id))))))]

    (-> d3
        (.select (-> config :canvas))
        (.on "click" (fn [] (on-node-click {:edges edges
                                            :config config
                                            :data data
                                            :nodeshash nodeshash
                                            :nodes nodes}))))

    (-> simulation (.nodes nodes))

    (-> simulation
        (.force "link")
        (.links edges))

    (draw-background config data)
    (-> data (aget "canvas-dimensions") (draw-field data config))
    (draw-graph {:edges edges
                 :config config
                 :nodeshash nodeshash
                 :nodes nodes})))
