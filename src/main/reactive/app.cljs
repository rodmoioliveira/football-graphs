(ns reactive.app
  (:require
    [reactive.config :refer [config mock-data]]
    [reactive.graph :refer [force-graph]]))

(def all-canvas (-> js/document
                    (.querySelectorAll ".canvas")
                    array-seq
                    ((fn [arr] (map #(.getAttribute % "id") arr)))))

(defn init []
  (doseq [id all-canvas]
    (force-graph {:data (clj->js mock-data) :config (config {:id id})})))

(defn ^:dev/after-load start []
  (init))

