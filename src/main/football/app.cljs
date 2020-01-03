(ns football.app
  (:require
    [football.data :refer [data]]
    [football.config :refer [config themes]]
    [football.mock-data :refer [mock-data]]
    [football.graph :refer [force-graph]]))

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
