(ns football.app
  (:require
    [football.data :refer [brazil switzerland data]]
    [football.utils :refer [assoc-pos]]
    [football.tatical :refer [tatical-schemes]]
    [football.config :refer [config themes]]
    [football.graph :refer [force-graph]]))

(def teams
  {:brazil brazil
   :switzerland switzerland})

(def all-canvas (-> js/document
                    (.querySelectorAll ".graph__canvas")
                    array-seq
                    (#(map (fn [el] {:id (.getAttribute el "id")
                                     :el el
                                     :formation (-> el (.getAttribute "data-formation") keyword)
                                     :data (-> el
                                               (.getAttribute "data-team")
                                               keyword
                                               teams)
                                     :theme (-> el (.getAttribute "data-theme") keyword)}) %))))

; (-> data clj->js js/console.log)

(defn set-nodes-pos
  [[{:keys [nodes links]} canvas formation tatical-schemes]]
  {:links links
   :nodes (assoc-pos
            canvas
            nodes
            formation
            tatical-schemes)})

(defn init []
  (doseq [canvas all-canvas]
    (let [formation (-> canvas :formation)
          el (-> canvas :el)
          data (-> canvas :data)
          format-data (-> [data el formation tatical-schemes] set-nodes-pos clj->js)]

      (force-graph {:data format-data
                    :config (config {:id (canvas :id)
                                     :theme (-> canvas :theme themes)})}))))

(defn reload! [] (init))
