(ns reactive.app
  (:require
    [reactive.data :refer [data]]
    [reactive.config :refer [config mock-data themes]]
    [reactive.graph :refer [force-graph]]))

(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [v] {:id (.getAttribute v "id")
                                    :theme (-> v (.getAttribute "data-theme") keyword)}) %))))

(-> data clj->js js/console.log)

(defn init []
  (doseq [canvas all-canvas]
    (force-graph {:data (clj->js mock-data)
                  :config (config {:id (canvas :id)
                                   :theme (-> canvas :theme themes)})})))

(defn ^:dev/after-load start []
  (init))
