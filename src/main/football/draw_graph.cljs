(ns football.draw-graph
  (:require
   ["d3" :as d3]
   [clojure.string :refer [split]]
   [utils.core :refer [get-distance radians-between find-node]]
   [football.store :refer [all-simulations
                           stop-simulations
                           make-active-node-store
                           update-active-node-store!
                           reset-active-node-store!]]
   [football.draw-field :refer [draw-field draw-background]]
   [football.simulations :refer [dragsubject dragged dragended dragstarted]]))

(set! *warn-on-infer* true)

(defn draw-edges
  "Draw the edges of passes between players."
  [{:keys [^js edge config nodeshash active-node-store]}]
  (let [source-x (-> edge .-source .-x)
        source-y (-> edge .-source .-y)
        target-x (-> edge .-target .-x)
        target-y (-> edge .-target .-y)
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
        active-edges (= (-> edge .-source .-id) @active-node-store)
        alpha-value (if (some? @active-node-store) (if active-edges 1 (-> config :edges :alpha)) 1)

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
      ; TODO: get color by atom!!!!
      ((fn [v] (set! (.-strokeStyle v) ((-> config :scales :edges->colors) value))))
      (.stroke)

      ; draw arrows
      (.beginPath)
      ; TODO: get color by atom!!!!
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
  (let [x-pos (-> node .-x)
        y-pos (-> node .-y)

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
                   :top (- y-pos 6 radius)
                   :bottom (+ y-pos 6 radius)}]
    (doto (-> config :ctx)
      ((fn [v] (set! (.-font v) (-> config :nodes :font :full))))
      ; TODO: get color by atom!!!!
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
  [{:keys [node config active-node-store]}]
  (let [x-pos (-> node .-x)
        y-pos (-> node .-y)
        is-active? (= (-> node .-id) @active-node-store)
      ; TODO: get color by atom!!!!
        active-color #(if is-active? (-> config :nodes :active :color) %)
        active-outline #(if is-active? (-> config :nodes :active :outline) %)

        ; Metrics for coloring node
        node-color-metric-name (-> config :nodes :node-color-metric name)
        node-color-metric-value (-> node .-metrics (#(aget % node-color-metric-name)))
        color-scale (-> config
                        :scales
                        (#(get-in % [(-> config :nodes :node-color-metric)]))
      ; TODO: get color by atom!!!!
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
      ; TODO: get color by atom!!!!
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
  [{:keys [edges nodes config nodeshash active-node-store]}]
  (doseq [e edges] (draw-edges {:edge e
                                :nodeshash nodeshash
                                :config config
                                :active-node-store active-node-store}))
  (doseq [n nodes] (draw-players {:node n
                                  :config config
                                  :active-node-store active-node-store})))

(defn get-canvas-current-dimensions
  [config]
  (-> config :canvas (.getBoundingClientRect)))

(defn on-node-click
  "On node click, only display that player passes network."
  [{:keys [edges
           nodes
           config
           nodeshash
           data
           active-node-store]}]
  (let [screen-width (-> js/window .-innerWidth)
        canvas-current-dimensions (get-canvas-current-dimensions config)
        x-domain #js [(- screen-width) (- (-> canvas-current-dimensions .-width) screen-width)]
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

    (if node
      (update-active-node-store! active-node-store (-> node .-id))
      (reset-active-node-store! active-node-store))

    (draw-background config data)
    (-> data (aget "canvas-dimensions") (draw-field data config))
    (draw-graph {:edges edges
                 :config config
                 :nodes nodes
                 :nodeshash nodeshash
                 :active-node-store active-node-store})))

(defn force-graph
  "Draw force graph elements."
  [{:keys [^js data config]}]
  (let [active-node-store (make-active-node-store)
        nodes (-> data .-nodes)
        canvas-current-dimensions (get-canvas-current-dimensions config)
        nodeshash (-> data ^:export .-nodeshash)
        min-passes-to-display (-> data (aget "graphs-options") (aget "min-passes-to-display"))
        filter-min-passes #(filter (fn [edge] (>= (-> edge .-value) min-passes-to-display)) %)
        edges (-> data .-links)
        simulation (-> d3
                       (.forceSimulation)
                       (.force "link" (-> d3
                                          (.forceLink)
                                          (.id (fn [d] (-> d .-id)))
                                          (.strength 0)))
                       (.force "charge" (-> d3 (.forceManyBody) (.strength 1)))
                       (.force "x"
                               (-> d3
                                   (.forceX)
                                   (.x (fn [^js d]
                                         (-> d .-id str (#(aget nodeshash %)) js->clj (get-in ["coord" "x"]))))
                                   (.strength 0.4)))
                       (.force "y"
                               (-> d3
                                   (.forceY)
                                   (.y (fn [^js d]
                                         (-> d .-id str (#(aget nodeshash %)) js->clj (get-in ["coord" "y"]))))
                                   (.strength 0.4)))
                       (.force
                        "collision"
                        (-> d3
                            (.forceCollide)
                            (.radius (fn [^js d]
                                       (let [node-radius-metric-name (-> config :nodes :node-radius-metric name)
                                             node-radius-metric-value (-> d .-metrics (#(aget % node-radius-metric-name)))
                                             radius-scale (-> config
                                                              :scales
                                                              (#(get-in % [(-> config :nodes :node-radius-metric)]))
                                                              (#(% :radius)))
                                             radius (radius-scale node-radius-metric-value)]
                                         radius))) (.strength 1)))
                       ; (.alphaDecay 0.00001)
                       (.velocityDecay 0.22))]

    (-> d3
        (.select (-> config :canvas))
        (.call
         (-> d3
             (.drag)
             (.container (-> config :canvas))
             (.subject (dragsubject
                        nodes
                        config
                        get-canvas-current-dimensions))
             (.on "start" (dragstarted simulation))
             (.on "drag" (dragged))
             (.on "end" (dragended simulation)))))

    (-> d3
        (.select (-> config :canvas))
        (.on "click" (fn []
                       (do
                         (on-node-click {:edges (-> edges filter-min-passes)
                                         :config config
                                         :data data
                                         :nodeshash nodeshash
                                         :nodes nodes
                                         :active-node-store active-node-store})))))

    (-> simulation
        (.nodes (->>
                 nodes
                 (map
                  #(-> js/Object
                       (.assign
                        %
                        #js {:x (/ (-> canvas-current-dimensions .-width) 2)}
                        #js {:y (/ (-> canvas-current-dimensions .-height) 2)})))
                 clj->js))
        (.on "tick" (fn []
                      (do
                        (js/console.log "tick" "animation-stores" (-> @all-simulations count))
                        (draw-background config data)
                        (-> data (aget "canvas-dimensions") (draw-field data config))
                        (draw-graph {:edges (-> edges filter-min-passes)
                                     :config config
                                     :nodeshash nodeshash
                                     :nodes nodes
                                     :active-node-store active-node-store})))))

    (-> simulation
        (.force "link")
        (.links edges))

    (draw-background config data)
    (-> data (aget "canvas-dimensions") (draw-field data config))
    (draw-graph {:edges (-> edges filter-min-passes)
                 :config config
                 :nodeshash nodeshash
                 :nodes nodes
                 :active-node-store active-node-store})
    (swap! all-simulations conj simulation)))
