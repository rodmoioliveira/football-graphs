(ns football.draw-graph
  (:require
   [clojure.string :refer [split]]
   [utils.core :refer [get-distance radians-between find-node]]
   ["d3" :as d3]))

(set! *warn-on-infer* true)
; ==================================
; Draw fns
; ==================================
(defn draw-edges
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
      (.setTransform))))

(defn draw-passes
  [obj]
  (-> obj :config :ctx (.save))
  (draw-edges obj)
  (-> obj :config :ctx (.restore)))

(defn draw-players-names
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
  [obj]
  (doto obj
    (draw-nodes)
    (draw-players-names)))

(defn draw-graph
  [{:keys [edges nodes config nodeshash active-node]}]
  (doseq [e edges] (draw-passes {:edge e
                                 :nodeshash nodeshash
                                 :config config
                                 :active-node active-node}))
  (doseq [n nodes] (draw-players {:node n
                                  :config config})))

(defn draw-background
  [^js config ^js data]
  (let [ctx (-> config :ctx)
        background-color (-> data .-field .-background)]
    (doto ctx
      (.clearRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height))
      ((fn [v] (set! (.-fillStyle v) background-color)))
      (.fillRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height)))))

; ==================================
; Soccer Field
; ==================================
(defn draw-field
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

      ; ==============
      ; borders
      ; ==============
      (.beginPath)
      (.moveTo padding padding)
      (.lineTo padding (- width padding))
      (.lineTo (- length padding) (- width padding))
      (.lineTo (- length padding) padding)
      (.lineTo padding padding)
      (.stroke)

      ; ==============
      ; corners
      ; ==============
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
      (.stroke)

      ; ==============
      ; midfield line
      ; ==============
      (.beginPath)
      (#(if flip?
          (doto %
            (.moveTo padding (/ width 2))
            (.lineTo (- length padding) (/ width 2)))
          (doto %
            (.moveTo (/ length 2) padding)
            (.lineTo (/ length 2) (- width padding)))))
      (.stroke)

      (#(if flip?
          (doto %
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

          (doto %
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

      ; ==============
      ; midfield circle
      ; ==============
      (.beginPath)
      (.arc
        (/ length 2)
        (/ width 2)
        (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
        0
        (* 2 js/Math.PI))
      (.stroke)

      ; ==============
      ; midfield point
      ; ==============
      (.beginPath)
      (.arc (/ length 2) (/ width 2) midfield-point-radius 0 (* 2 js/Math.PI))
      (.fill))))


; ==================================
; Events
; ==================================
(defn clicked
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

; ==================================
; Force graph
; ==================================
(defn force-graph
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
        (.on "click" (fn [] (clicked {:edges edges
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
