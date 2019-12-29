(ns
  ^{:doc "Graphs for football matches"
    :author "Rodolfo Mói"
    :inspiration "Inspiration from https://tsj101sports.com/2018/06/20/football-with-graph-theory/"}
  reactive.graph
  (:require
    [reactive.utils :refer [get-distance find-point radians-between]]
    ["d3" :as d3]))

; ==================================
; Draw fns
; ==================================
(defn draw-edges
  [{:keys [edge config]}]
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

    (doto (-> config :ctx)
      ; translate to source node center point
      (.translate source-x source-y)
      ; rotate canvas by that angle
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
      ; ; restore canvas
      (.setTransform))))

(defn draw-passes
  [{:keys [edge config]}]
  (-> config :ctx (.save))
  (draw-edges {:edge edge :config config})
  (-> config :ctx (.restore)))

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
        active-color #(if is-active? "red" %)]
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
  [{:keys [node config]}]
  (doto {:node node :config config}
    (draw-nodes)
    (draw-numbers)))

(defn draw-graph
  [{:keys [edges nodes config]}]
  (let [ctx (-> config :ctx)]
    (doto ctx
      (.save)
      (.clearRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height))
      ((fn [v] (set! (.-fillStyle v) "white")))
      (.fillRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height)))
    (doseq [e edges] (draw-passes {:edge e :config config}))
    (doseq [n nodes] (draw-players {:node n :config config}))
    (-> ctx (.restore))))

; ==================================
; Events
; ==================================
(defn find-node
  [nodes x y radius]
  (let [rsq (* radius radius)
        nodes-length (-> nodes count dec)]
    (loop [i 0]
      (let [interate? (< i nodes-length)
            node (get nodes i)
            dx (- x (-> node .-initial_pos .-x))
            dy (- y (-> node .-initial_pos .-y))
            dist-sq (+ (* dx dx) (* dy dy))
            node-found? (< dist-sq rsq)]
        (if node-found?
          node
          (if interate? (-> i inc recur)))))))

(defn clicked
  [{:keys [edges nodes config]}]
  (let [x (or (-> d3 .-event .-layerX) (-> d3 .-event .-offsetX))
        y (or (-> d3 .-event .-layerY) (-> d3 .-event .-offsetY))
        node (find-node nodes x y (-> config :nodes :radius))]
    (doseq [n nodes] (set! (.-active n) false))
    (if node (set! (.-active node) true))
    (draw-graph {:edges edges
                 :config config
                 :nodes nodes})))

; ==================================
; Force graph
; ==================================
(defn force-graph
  [{:keys [data config]}]
  (let [nodes (-> data .-nodes)
        edges (-> data .-links)
        click-event (-> d3
                        (.select (-> config :canvas))
                        (.on "click" (fn [] (clicked {:edges edges
                                                      :config config
                                                      :nodes nodes}))))
        simulation (-> d3
                       (.forceSimulation)
                       (.force "link" (-> d3
                                          (.forceLink)
                                          (.id (fn [d] (-> d .-id))))))]

    (-> simulation
        (.nodes nodes)
        (.on "tick" (fn [] (draw-graph {:edges edges
                                        :config config
                                        :nodes nodes}))))
    (-> simulation
        (.force "link")
        (.links edges))))

