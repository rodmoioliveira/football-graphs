(ns
  ^{:doc "Graphs for football matches"
    :author "Rodolfo Mói"}
  reactive.graph
  (:require
    [reactive.utils :refer [get-distance radians-between find-node]]
    ["d3" :as d3]))

; ==================================
; Draw fns
; ==================================
(defn draw-edges
  [{:keys [edge config active-node]}]
  (let [source-x (-> edge .-source .-initial_pos .-x)
        source-y (-> edge .-source .-initial_pos .-y)
        target-x (-> edge .-target .-initial_pos .-x)
        target-y (-> edge .-target .-initial_pos .-y)
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
        alpha-value (if active-node (if active-edges 1 0.03) 1)]

    (doto (-> config :ctx)
      ; translate to source node center point
      (.translate source-x source-y)
      ; rotate canvas
      (.rotate orientation)
      ; translate again between edges
      (.translate 0 (-> config :edges :distance-between))
      ; draw edges
      (.beginPath)
      (.moveTo (-> config :nodes :radius (+ (-> config :edges :padding))) 0)
      (.lineTo
        (-> base-vector
            first
            (- (-> config :nodes :radius)
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
        (-> base-vector first (- (-> config :nodes :radius) (-> config :edges :padding)))
        (-> base-vector second))
      (.lineTo
        (-> base-vector first (- (-> config :arrows :width)))
        (* ((-> config :scales :edges->width) value) (-> config :arrows :expansion)))
      (.lineTo
        (-> base-vector first (- (-> config :arrows :width)))
        (- (* ((-> config :scales :edges->width) value) (-> config :arrows :expansion))))
      (.fill)

      ; restore canvas
      (.setTransform))))

(defn draw-passes
  [obj]
  (-> obj :config :ctx (.save))
  (draw-edges obj)
  (-> obj :config :ctx (.restore)))

(defn draw-numbers
  [{:keys [node config]}]
  (let [x-initial-pos (-> node .-initial_pos .-x)
        y-initial-pos (-> node .-initial_pos .-y)]
    (doto (-> config :ctx)
      ((fn [v] (set! (.-font v) (-> config :nodes :font :full))))
      ((fn [v] (set! (.-fillStyle v) (-> config :nodes :font :color))))
      ((fn [v] (set! (.-textAlign v) (-> config :nodes :font :text-align))))
      ((fn [v] (set! (.-textBaseline v) (-> config :nodes :font :base-line))))
      (.fillText (-> node .-id) x-initial-pos y-initial-pos))))

(defn draw-nodes
  [{:keys [node config]}]
  (let [x-initial-pos (-> node .-initial_pos .-x)
        y-initial-pos (-> node .-initial_pos .-y)
        is-active? (-> node .-active)
        active-color #(if is-active? (-> config :nodes :active :color) %)]
    (doto (-> config :ctx)
      (.beginPath)
      (.moveTo (+ x-initial-pos (-> config :nodes :radius)) y-initial-pos)
      (.arc x-initial-pos y-initial-pos (-> config :nodes :radius) 0 (* 2 js/Math.PI))
      ((fn [v] (set! (.-fillStyle v) (-> config :nodes :fill :color active-color))))
      (.fill)
      ((fn [v] (set! (.-strokeStyle v) (-> config :nodes :outline :color))))
      ((fn [v] (set! (.-lineWidth v) (-> config :nodes :outline :width))))
      (.stroke))))

(defn draw-players
  [obj]
  (doto obj
    (draw-nodes)
    (draw-numbers)))

(defn draw-graph
  [{:keys [edges nodes config active-node]}]
  (let [ctx (-> config :ctx)]
    (doto ctx
      (.save)
      (.clearRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height))
      ((fn [v] (set! (.-fillStyle v) "white")))
      (.fillRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height)))
    (doseq [e edges] (draw-passes {:edge e
                                   :config config
                                   :active-node active-node}))
    (doseq [n nodes] (draw-players {:node n
                                    :config config}))
    (-> ctx (.restore))))

; ==================================
; Events
; ==================================
(defn clicked
  [{:keys [edges nodes config]}]
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
        node (find-node nodes (mapping-x x) (mapping-y y) (-> config :nodes :radius))]

    ; TODO: implement toogle feature
    (doseq [n nodes] (set! (.-active n) false))
    (when node (set! (.-active node) (not (-> node .-active))))

    (draw-graph {:edges edges
                 :config config
                 :nodes nodes
                 :active-node node})))

; ==================================
; Force graph
; ==================================
(defn force-graph
  [{:keys [data config]}]
  (let [nodes (-> data .-nodes)
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
                                      :nodes nodes}))))

    (-> simulation (.nodes nodes))

    (-> simulation
        (.force "link")
        (.links edges))

    (draw-graph {:edges edges
                 :config config
                 :nodes nodes})))
