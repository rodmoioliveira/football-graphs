(ns football.data
  (:require
   [shadow.resource :as rc]
   [clojure.set :refer [project]]
   [football.utils :refer [hash-by logger]]))

(def matches (-> js/JSON
                 (.parse (rc/inline "../data/matches_World_Cup.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (filter #(= (-> % :wyId) 2057978) v)))
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(def players (-> js/JSON
                 (.parse (rc/inline "../data/players.json"))
                 (js->clj :keywordize-keys true)
                 ((fn [v] (reduce (partial hash-by :wyId) (sorted-map) v)))))

(def nodes (-> players
               vals
               (project [:pos :currentNationalTeamId])
               (#(map (fn [p] (merge p {:id (p :pos) :pos (-> p :pos keyword)})) %))
               (#(group-by :currentNationalTeamId %))
               vals
               reverse))

(defn links
  []
  (let [group-by-id (fn [v] (group-by :teamId v))
        assoc-player-data #(assoc-in % [:pos] (get-in players [(-> % :playerId) :pos]))]
    (-> js/JSON

        ; ####################################
        ; FIXME: Fix logic of passes
        ; ####################################
        (.parse (rc/inline "../data/events.json"))
        (js->clj :keywordize-keys true)
        ; Other events must be consider for passing network...
        ((fn [v] (filter #(= (-> % :eventId) 8) v)))
        logger
        ((fn [p] (map assoc-player-data p)))
        group-by-id
        vals
        ((fn [group] (map #(partition 2 1 %) group)))
        ((fn [teams] (map (fn [link]
                            (map (fn [[source target]]
                                   {:source (get-in source [:pos])
                                    :target (get-in target [:pos])
                                    :teamId (get-in source [:teamId])}) link)) teams)))
        ; ####################################
        ; ####################################
        ; ####################################

        ((fn [teams] (map frequencies teams)))
        ((fn [teams] (map (fn [team] (map (fn [[ks v]] (merge ks {:value v})) team)) teams)))
        ((fn [teams] (map #(sort-by :value %) teams))))))

(def data
  {:links (links)
   :matches matches
   :players players
   :nodes nodes})

(def brazil
  {:links (first (links))
   :nodes (first nodes)})

(def switzerland
  {:links (last (links))
   :nodes (last nodes)})
