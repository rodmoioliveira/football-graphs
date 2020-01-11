; clj src/main/io/spit_game.clj --id=2057978
(ns io.spit-game
  (:require
   [camel-snake-kebab.core :as csk]
   ; FIXME: import cljc in clj...
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

(def options [["-i" "--id ID" "Game ID"]
              ["-t" "--type TYPE" "File Type (json or edn)"
               :default :edn
               :parse-fn keyword
               :validate [#(or (= % :edn) (= % :json)) "Must be json or edn"]]])
(def args (-> *command-line-args* (parse-opts options)))
(def id (-> args :options :id edn/read-string))
(def id-keyword (-> id str keyword))
(def file-type (-> args :options :type))
(def errors (-> args :errors))

(defn get-data
  []
  (let [path "main/data/soccer_match_event_dataset/"
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

(def output-file-type
  {:edn #(-> % pp/pprint with-out-str)
   :json #(-> % (json/write-str :key-fn (fn [k] (-> k name str csk/->camelCase))))})

(if (-> errors some? not)
  (let [data (get-data)]
    (spit
     (str "src/main/data/games/" (-> data :match :label csk/->snake_case)  "." (name file-type))
     ((output-file-type file-type) data))
    (print "Success!"))
  (print errors))
