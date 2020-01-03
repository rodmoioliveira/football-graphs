(ns reactive.data
  (:require
    [shadow.resource :as rc]
    [reactive.utils :refer [hash-by]]))

(def matches (-> js/JSON
                 (.parse (rc/inline "./data/matches_World_Cup.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (filter #(= (-> % :wyId) 2057978) v)))
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(def players (-> js/JSON
                 (.parse (rc/inline "./data/players.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(defn passes-from-2057958
  []
  (let [group-by-id (fn [v] (group-by :teamId v))]
    (-> js/JSON
        (.parse (rc/inline "./data/passes-2057958.json"))
        (js->clj :keywordize-keys true)
        (group-by-id)
        vals
        ((fn [vals] (map #(partition 2 %) vals))))))

(def data
  {:passes {:2057958 (passes-from-2057958)}
   :matches matches
   :players players})
