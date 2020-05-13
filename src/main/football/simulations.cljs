(ns football.simulations
  (:require
   ["d3" :as d3]
   [utils.core :refer [find-node]]))

(def transform (-> d3 .-zoomIdentity))

(defn dragsubject
  [nodes
   config
   canvas-current-dimensions]
  (fn []
    (let [
          x (-> transform (.invertX (-> d3 .-event .-x)))
          y (-> transform (.invertY (-> d3 .-event .-y)))
          node (find-node
                config
                (-> canvas-current-dimensions .-width)
                nodes
                x
                y)]
      node)))

(defn dragstarted
  [simulation]
  (fn []
    (do
      (when (-> d3 .-event .-active)
        (-> simulation (.alphaTarget 1) (.restart)))
      (-> d3 .-event .-subject .-fx (set! (-> d3 .-event .-x)))
      (-> d3 .-event .-subject .-fy (set! (-> d3 .-event .-y))))))

(defn dragged
  []
  (fn []
    (do
      (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
      (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y))))))))

(defn dragended
  [simulation]
  (fn []
    (do
      (when-not (-> d3 .-event .-active)
        (-> simulation (.alphaTarget 0)))
      (-> d3 .-event .-subject .-fx (set! nil))
      (-> d3 .-event .-subject .-fy (set! nil)))))
