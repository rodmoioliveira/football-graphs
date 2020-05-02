(ns io.spit-match
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.pprint :refer [pprint]]

   [utils.core :refer [hash-by output-file-type assoc-names hash-by-id]]))

; ==================================
; Command Line Options
; ==================================
(def options [["-i" "--id ID" "Match ID"]
              ["-t" "--type TYPE" "File Type (json or edn)"
               :default :edn
               :parse-fn keyword
               :validate [#(or (= % :edn) (= % :json)) "Must be json or edn"]]])
(def args (-> *command-line-args* (parse-opts options)))
(def id (-> args :options :id edn/read-string))
(def id-keyword (-> id str keyword))
(def file-type (-> args :options :type))
(def errors (-> args :errors))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  []
  (let [path "data/soccer_match_event_dataset/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        tags (-> (io/resource "data/meta/meta.edn")
                 slurp
                 edn/read-string
                 :tags)
        match (-> (get-file "matches_World_Cup.json")
                  slurp
                  json->edn
                  hash-by-id
                  id-keyword)
        teams-ids (-> match
                      :teams-data
                      vals
                      (#(map (fn [{:keys [team-id]}] team-id) %)))
        reduce-by (fn [prop v] (reduce (partial hash-by prop) (sorted-map) v))
        events-filtered (-> (get-file "events_World_Cup.json")
                            slurp
                            json->edn
                            (#(filter (fn [e] (= (-> e :match-id) id)) %)))
        players-national-teams-hash (->> events-filtered
                                         (map (fn [{:keys [player-id team-id]}]
                                                {:player-id player-id :team-id team-id}))
                                         (reduce-by :player-id))
        players-in-match (->> events-filtered (map :player-id) set)
        events (-> events-filtered
                   (#(map (fn [e]
                            (assoc
                             e
                             :tags
                             (map (fn [t]
                                    (assoc
                                     t
                                     :description
                                     (get-in tags [(-> t :id str keyword) :description])))
                                  (-> e :tags))))
                          %)))
        players (-> (get-file "players.json")
                    slurp
                    json->edn
                    ; TODO: filtrar os jogadores com base nos eventos!
                    (#(filter (fn [{:keys [wy-id current-national-team-id]}]
                                (or
                                 (some (fn [id] (= id current-national-team-id)) teams-ids)
                                 (some (set [wy-id]) players-in-match)))
                              %))
                    (#(map (fn [p] (assoc
                                    p
                                    :current-national-team-id
                                    (if (= (-> p :current-national-team-id) "null")
                                      (get-in players-national-teams-hash [(-> p :wy-id str keyword) :team-id])
                                      (-> p :current-national-team-id)))) %))
                    hash-by-id)]
    {:match (->> match
                 (assoc-names players)
                 (reduce-by :team-id)
                 (#(assoc match :teams-data %)))
     :players players
     :events events}))

; ==================================
; IO
; ==================================
(if (-> errors some? not)
  (let [data (get-data)
        match-label (-> data :match :label csk/->snake_case)
        dist "src/main/data/matches/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) data))
    (println (str "Success on spit " dist match-label "." ext)))
  (print errors))
