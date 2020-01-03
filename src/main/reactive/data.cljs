(ns reactive.data
  (:require
    [shadow.resource :as rc]
    [reactive.utils :refer [hash-by]]))

(def matches (-> js/JSON
                 (.parse (rc/inline "../data/matches_World_Cup.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (filter #(= (-> % :wyId) 2057978) v)))
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(def players (-> js/JSON
                 (.parse (rc/inline "../data/players.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(defn passes
  []
  (let [group-by-id (fn [v] (group-by :teamId v))
        assoc-player-data #(assoc-in % [:playerData] (get-in players [(-> % :playerId)]))]
    (-> js/JSON
        (.parse (rc/inline "../data/passes.json"))
        (js->clj :keywordize-keys true)
        ((fn [p] (map assoc-player-data p)))
        group-by-id
        vals
        ((fn [group] (map #(partition 2 1 %) group))))))

(def data
  {:passes (passes)
   :matches matches
   :players players})
