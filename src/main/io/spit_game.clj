; clj src/main/io/spit_game.clj --id=2057978
(ns io.spit-game
  (:require
   [camel-snake-kebab.core :as csk]
   ; [football.utils :refer [hash-by]]
   [clojure.edn :as edn]
   [clojure.pprint :as pp]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

(defn hash-by
  "Hashmap a collection by a given key"
  [key acc cur]
  (assoc acc (-> cur key str keyword) cur))

(def options [["-i" "--id ID" "Game ID"]])
(def args (-> *command-line-args* (parse-opts options)))
(def id (-> args :options :id edn/read-string))
(def id-keyword (-> id str keyword))

(defn get-data
  []
  (let [path "main/data/"
        get-file #(io/resource (str path %))
        list->hash (fn [v] (reduce (partial hash-by :wy-id) (sorted-map) v))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        match (-> (get-file "matches_World_Cup.json")
                  slurp
                  json->edn
                  list->hash
                  id-keyword)
        players (-> (get-file "players.json")
                    slurp
                    json->edn)
        teams-ids (-> match
                      :teams-data
                      vals
                      (#(map (fn [{:keys [team-id]}] team-id) %)))]

    {:match match
     :players (-> players
                  (#(filter (fn [{:keys [current-national-team-id]}]
                              (some (fn [id] (= id current-national-team-id)) teams-ids))
                            %))
                  list->hash)
     :events (-> (get-file "events_World_Cup.json")
                 slurp
                 json->edn
                 (#(filter (fn [e] (= (-> e :match-id) id)) %)))}))

(def data (get-data))

(spit
 (str "src/main/data/games/" (-> data :match :label csk/->snake_case) ".edn")
 (-> data pp/pprint with-out-str))
