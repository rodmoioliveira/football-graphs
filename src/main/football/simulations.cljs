(ns football.simulations
  (:require
   ["d3" :as d3]
   [utils.core :refer [find-node]]))

(def transform (-> d3 .-zoomIdentity))

(defn dragsubject
  [nodes
   config
   get-canvas-current-dimensions]
  (fn []
    (let [canvas-current-dimensions (get-canvas-current-dimensions config)
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
          x (-> transform (.invertX (-> d3 .-event .-x)))
          y (-> transform (.invertY (-> d3 .-event .-y)))
          node (find-node
                config
                (-> canvas-current-dimensions .-width)
                nodes
                (mapping-x x)
                (mapping-y y))]
      node)))

(defn dragstarted
  [simulation]
  (fn []
    (when (-> d3 .-event .-active)
      (-> simulation (.alphaTarget 1) (.restart)))
    (-> d3 .-event .-subject .-fx (set! (-> d3 .-event .-x)))
    (-> d3 .-event .-subject .-fy (set! (-> d3 .-event .-y)))))

(defn dragged
  []
  (fn []
    (-> d3 .-event .-subject .-fx (set! (-> transform (.invertY (-> d3 .-event .-x)))))
    (-> d3 .-event .-subject .-fy (set! (-> transform (.invertX (-> d3 .-event .-y)))))))

(defn dragended
  [simulation]
  (fn []
    (-> simulation (.alphaTarget 0))
    (-> d3 .-event .-subject .-fx (set! nil))
    (-> d3 .-event .-subject .-fy (set! nil))))
