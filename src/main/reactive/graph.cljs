(ns reactive.graph
  (:require ["d3" :as d3]))

(def canvas
  (-> js/document
      (.getElementById "canvas")))

(js/console.log (.-height canvas) (.-width canvas))

; function findNode(nodes, x, y, radius) {
;     const rSq = radius * radius;
;     let i;
;     for (i = nodes.length - 1; i >= 0; --i) {
;       const node = nodes[i],
;             dx = x - node.x,
;             dy = y - node.y,
;             distSq = (dx * dx) + (dy * dy);
;       if (distSq < rSq) {
;         return node;
;       }
;     }
;     // No node selected
;     return undefined;
;   }
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

; function forceSimulation(width, height) {
;   return d3.forceSimulation()
;     .force("center", d3.forceCenter(width / 2, height / 2))
;     .force("charge", d3.forceManyBody())
;     .force("link", d3.forceLink().id(d => d.id));
; }
(defn force-simulation
  [width height]
  (-> d3
      (.forceSimulation)
      (.force "center" (d3/forceCenter (/ width 2) (/ height 2)))
      (.force "change" (d3/forceManyBody))
      (.force "link" (-> d3 (.forceLink) (.id (fn [{:keys [id]}] id))))))

(defn force-graph
  [data]
  (js/console.log data)
  (js/console.log (find-node #js [#js {:x 315 :y 378} {:x 314 :y 378}] 10 10 200)))

; color = {
;   const scale = d3.scaleOrdinal(d3.schemeCategory10);
;   return d => scale(d.group);
; }
(defn color
  [{:keys [group]}]
  ((d3/scaleOriginal d3/schemeCategory10) group))


; const height = 600
(def height 600)

; const data = d3.json("https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json")
(def get-data
  (-> d3
      (.json "https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json")))

; https://observablehq.com/d/42f72efad452c2f0
(defn init-graph
  []
  (-> get-data (.then force-graph))
  ; (js/console.log color)
  (js/console.log "Iniciando grafo..."))
