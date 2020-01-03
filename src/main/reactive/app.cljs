(ns reactive.app
  (:require
    [reactive.data :refer [data]]
    [reactive.config :refer [config themes]]
    [reactive.mock-data :refer [mock-data]]
    [reactive.graph :refer [force-graph]]))

(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [el] {:id (.getAttribute el "id")
                                    :el el
                                    :formation (-> el (.getAttribute "data-formation") keyword)
                                    :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

(-> data clj->js js/console.log)

(defn init []
  (doseq [canvas all-canvas]
    (force-graph {:data (clj->js (mock-data (-> canvas :el) (-> canvas :formation)))
                  :config (config {:id (canvas :id)
                                   :theme (-> canvas :theme themes)})})))

(defn reload! [] (init))
