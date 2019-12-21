(ns reactive.app
  (:require [reactive.rxjs :refer [move-mouse$]]
            [reactive.graph :refer [init-graph]]))

(defn init []
  (init-graph)
  (move-mouse$))

