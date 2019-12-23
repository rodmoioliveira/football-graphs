(ns reactive.graph
  (:require ["d3" :as d3]))

(def scale (-> d3 (.scaleOrdinal (-> d3 (.-schemeCategory10)))))
(def color (fn [d] (scale (-> d .-group))))
(def canvas (-> js/document (.getElementById "canvas")))
(def ctx (-> canvas (.getContext "2d")))
(def w2 (/ (.-height canvas) 2))
(def h2 (/ (.-width canvas) 2))
(def node-radius 20)
(def transform (-> d3 .-zoomIdentity))

(defn find-node
  [nodes x y radius]
  (let [rsq (* radius radius)
        nodes-length (-> nodes count dec)]
    (loop [i 0]
      (let [interate? (< i nodes-length)
            node (get nodes i)
            dx (- x (.-x node))
            dy (- y (.-y node))
            dist-sq (+ (* dx dx) (* dy dy))
            node-found? (< dist-sq rsq)]
        (if node-found?
          node
          (if interate? (-> i inc recur)))))))

(defn force-simulation
  [width height]
  (-> d3
      (.forceSimulation)
      (.force "center" (-> d3 (.forceCenter (/ width 2) (/ height 2))))
      (.force "change" (-> d3 (.forceManyBody)))
      (.force "link" (-> d3 (.forceLink) (.distance 120) (.id (fn [d] (-> d .-id)))))))

(def simulation (force-simulation (.-width canvas) (.-height canvas)))

(defn update-coords
  [node]
  (-> node .-x (set! (-> transform (.applyX (-> node .-x)))))
  (-> node .-y (set! (-> transform (.applyY (-> node .-y))))))

(defn drag-subject
  [nodes]
  (let [x (-> transform (.invertX (-> d3 .-event .-x)))
        y (-> transform (.invertY (-> d3 .-event .-y)))
        node (find-node nodes x y node-radius)]
    (if node
      (update-coords node))
    node))

(defn drag-started
  []
  (-> simulation (.alphaTarget 1) (.restart))
  (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
  (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y))))))

(defn dragged
  []
  (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
  (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y))))))

(defn dragended
  []
  (-> simulation (.alphaTarget 0))
  (-> d3 .-event .-subject .-fx (set! nil))
  (-> d3 .-event .-subject .-fy (set! nil)))

(defn draw-edges
  [edge]
  (doto ctx
    (.beginPath)
    (.moveTo (-> edge .-source .-x) (-> edge .-source .-y))
    (.lineTo (-> edge .-target .-x) (-> edge .-target .-y))
    ((fn [v] (set! (.-lineWidth v) (js/Math.sqrt (-> edge .-value)))))
    ((fn [v] (set! (.-strokeStyle v) "#fff")))
    (.stroke)))

(defn draw-nodes
  [node]
  (doto ctx
    (.beginPath)
    (.moveTo (+ (-> node .-x) node-radius) (-> node .-y))
    (.arc (-> node .-x) (-> node .-y) node-radius 0 (* 2 js/Math.PI))
    ((fn [v] (set! (.-fillStyle v) (color node))))
    (.fill)
    ((fn [v] (set! (.-font v) "20px sans-serif")))
    ((fn [v] (set! (.-fillStyle v) "#fff")))
    ((fn [v] (set! (.-textAlign v) "center")))
    (.fillText (-> node .-id) (-> node .-x) (-> node .-y (+ 5)))
    ((fn [v] (set! (.-strokeStyle v) "#fff")))
    ((fn [v] (set! (.-lineWidth v) "1.5")))
    (.stroke)))

(defn simulation-update
  [edges nodes]
  (doto ctx
    (.save)
    (.clearRect 0 0 (.-width canvas) (.-height canvas))
    (.translate (-> transform .-x) (-> transform .-y))
    (.scale (-> transform .-k) (-> transform .-k)))
  (doseq [e edges] (draw-edges e))
  (doseq [n nodes] (draw-nodes n))
  (-> ctx (.restore)))

(defn force-graph
  [data]
  (let [nodes (-> data .-nodes)
        edges (-> data .-links)]
    (-> d3
        (.select canvas)
        (.call (-> d3
                   (.drag)
                   (.container canvas)
                   (.subject (fn [] (drag-subject nodes)))
                   (.on "start" drag-started)
                   (.on "drag" dragged)
                   (.on "end" dragended))))

    (-> simulation
        (.nodes nodes)
        (.on "tick" (fn [] (simulation-update edges nodes))))

    (-> simulation
        (.force "link")
        (.links edges))))

(def get-data
  (-> d3
      (.json "https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json")))

; TODO: estabelecer posicionamento inicial dos nodes
; https://bl.ocks.org/mbostock/3750558
(def mock-data
  {
   :nodes [
           {:id "a" :group 1}
           {:id "b" :group 1}
           {:id "c" :group 1}
           ]
   :links [
           {:source "a" :target "b" :value 1}
           {:source "b" :target "a" :value 1}
           {:source "a" :target "c" :value 10}
           {:source "c" :target "a" :value 10}
           ]
   })

; https://observablehq.com/d/42f72efad452c2f0
; (defn init-graph [] (-> get-data (.then force-graph)))
(defn init-graph [] (-> mock-data clj->js force-graph))
