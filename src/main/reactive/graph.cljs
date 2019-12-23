(ns reactive.graph
  (:require ["d3" :as d3]))

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
      (.force "center" (-> d3 (.forceCenter (/ width 2) (/ height 2))))
      (.force "change" (-> d3 (.forceManyBody)))
      (.force "link" (-> d3 (.forceLink) (.id (fn [d] (-> d .-id)))))))

; color = {
;   const scale = d3.scaleOrdinal(d3.schemeCategory10);
;   return d => scale(d.group);
; }
(def scale (-> d3 (.scaleOrdinal (-> d3 (.-schemeCategory10)))))
(def color (fn [d] (scale (-> d .-group))))

(def canvas
  (-> js/document
      (.getElementById "canvas")))

(def ctx
  (-> canvas
      (.getContext "2d")))

; const w2 = width / 2,
;       h2 = height / 2,
;       nodeRadius = 10;
(def w2 (/ (.-height canvas) 2))
(def h2 (/ (.-width canvas) 2))
(def node-radius 10)

; (doto ctx
;   ((fn [v] (set! (.-lineWidth v) 10)))
;   (.strokeRect 75 140 150 100)
;   (.fillRect 130 190 40 60)
;   (.moveTo 50 140)
;   (.lineTo 150 60)
;   (.lineTo 250 140)
;   (.closePath)
;   (.stroke)
;   )

; let transform = d3.zoomIdentity;
(def transform (-> d3 .-zoomIdentity))

; const simulation = forceSimulation(width, height);
(def simulation
  (force-simulation (.-width canvas) (.-height canvas)))

; /** Find the node that was clicked, if any, and return it. */
; function dragSubject() {
;   const x = transform.invertX(d3.event.x),
;         y = transform.invertY(d3.event.y);
;   const node = findNode(nodes, x, y, nodeRadius);
;   if (node) {
;     node.x =  transform.applyX(node.x);
;     node.y = transform.applyY(node.y);
;   }
;   // else: No node selected, drag container
;   return node;
; }
(defn update-coords
  [node]
  (do
    (-> node .-x (set! (-> transform (.applyX (-> node .-x)))))
    (-> node .-y (set! (-> transform (.applyY (-> node .-y))))))
  )

(defn drag-subject
  [nodes]
  (let [x (-> transform (.invertX (-> d3 .-event .-x)))
        y (-> transform (.invertY (-> d3 .-event .-y)))
        node (find-node nodes x y node-radius)]
    (if node
      (update-coords node))
    node))

; function dragStarted() {
;   if (!d3.event.active) {
;     simulation.alphatarget(0.3).restart();
;   }
;   d3.event.subject.fx = transform.invertx(d3.event.x);
;   d3.event.subject.fy = transform.inverty(d3.event.y);
; }
(defn drag-started
  []
  (if (not (-> d3 .-event .-active))
    (-> simulation (.alphatarget 0.3) (.restart)))
  (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
  (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y))))))

; function dragged() {
;   d3.event.subject.fx = transform.invertx(d3.event.x);
;   d3.event.subject.fy = transform.inverty(d3.event.y);
; }
(defn dragged
  []
  (do
    (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
    (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y)))))))

; function dragended() {
;   if (!d3.event.active) {
;     simulation.alphatarget(0);
;   }
;   d3.event.subject.fx = null;
;   d3.event.subject.fy = null;
; }
(defn dragended
  []
  (do
    (if (not (-> d3 .-event .-active))
      (-> simulation (.alphatarget 0)))
    (-> d3 .-event .-subject .-fx (set! nil))
    (-> d3 .-event .-subject .-fy (set! nil))
    ))

; function drawEdges(d) {
;   ctx.beginPath();
;   ctx.moveTo(d.source.x, d.source.y);
;   ctx.lineTo(d.target.x, d.target.y);
;   ctx.lineWidth = Math.sqrt(d.value);
;   ctx.strokeStyle = '#aaa';
;   ctx.stroke();
; }
(defn draw-edges
  [edge]
  (doto ctx
    (.beginPath)
    (.moveTo (-> edge .-source .-x) (-> edge .-source .-y))
    (.lineTo (-> edge .-target .-x) (-> edge .-target .-y))
    ((fn [v] (set! (.-lineWidth v) (js/Math.sqrt (-> edge .-value)))))
    ((fn [v] (set! (.-strokeStyle v) "#aaa")))
    (.stroke)))

; function drawNodes(d) {
;   ctx.beginPath();
;   // Node fill
;   ctx.moveTo(d.x + nodeRadius, d.y);
;   ctx.arc(d.x, d.y, nodeRadius, 0, 2 * Math.PI);
;   ctx.fillStyle = color(d);
;   ctx.fill();
;   // Node outline
;   ctx.strokeStyle = '#fff'
;   ctx.lineWidth = '1.5'
;   ctx.stroke();
; }
(defn draw-nodes
  [node]
  (doto ctx
    (.beginPath)
    (.moveTo (+ (-> node .-x) node-radius) (-> node .-y))
    (.arc (-> node .-x) (-> node .-y) node-radius 0 (* 2 js/Math.PI))
    ((fn [v] (set! (.-fillStyle v) (color node))))
    (.fill)
    ((fn [v] (set! (.-strokeStyle v) "#fff")))
    ((fn [v] (set! (.-lineWidth v) "1.5")))
    (.stroke)))

; function simulationUpdate() {
;   ctx.save();
;   ctx.clearRect(0, 0, width, height);
;   ctx.translate(transform.x, transform.y);
;   ctx.scale(transform.k, transform.k);

;   // Draw edges
;   edges.forEach(drawEdges);

;   // Draw nodes
;   nodes.forEach(drawNodes);
;   ctx.restore();
; }
(defn simulation-update
  [edges nodes]
  (js/console.log "arui")
  (do
    (doto ctx
      (.save)
      (.clearRect 0 0 (.-width canvas) (.-height canvas))
      (.translate (-> transform .-x) (-> transform .-y))
      (.scale (-> transform .-k) (-> transform .-k)))
    (doseq [e edges] (draw-edges e))
    (doseq [n nodes] (draw-nodes n))
    )
  )

; d3.select(canvas)
;   .call(d3.drag()
;         .container(canvas)
;         .subject(() => dragSubject(nodes))
;         .on('start', () => dragStarted())
;         .on('drag', () => dragged())
;         .on('end', () => dragEnded()));

; simulation.nodes(nodes)
;   .on("tick",() => simulationUpdate(edges, nodes));

; simulation.force("link")
;   .links(edges);
(defn force-graph
  [data]
  (let [nodes (-> data .-nodes)
        edges (-> data .-links)]
    (do
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
          (.links edges)
          )
      )

    ))

; const data = d3.json("https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json")
(def get-data
  (-> d3
      (.json "https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json")))

; https://observablehq.com/d/42f72efad452c2f0
(defn init-graph
  []
  (-> get-data (.then force-graph)))
