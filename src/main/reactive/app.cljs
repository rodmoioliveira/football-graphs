(ns reactive.app
  (:require [reactive.rxjs :refer [move-mouse$]]
            [reactive.graph :refer [init-graph]]))

; https://observablehq.com/d/42f72efad452c2f0
(defn init []
  (init-graph)
  (move-mouse$))

