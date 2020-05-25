(ns io.spit-match
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.string :as s]
   [clojure.pprint :refer [pprint]]

   [utils.core :refer [hash-by
                       output-file-type
                       assoc-names
                       deaccent
                       hash-by-id
                       championships]]))

; ==================================
; Command Line Options
; ==================================
(def options [["-i" "--id ID" "Match ID"]
              ["-c" "--championship CHAMPIONSHIP" "Championship"
               :parse-fn str
               :validate [#(some? (some #{%} championships))
                          (str "Must be a valid championship " championships)]]])
(def args (-> *command-line-args* (parse-opts options)))
(def ids (-> args :options :id (s/split #" ") (#(map (fn [id] (-> id Integer.)) %))))
(def ids-keyword (->> ids (map str) (map keyword)))
(def championship (-> args :options :championship))
(def errors (-> args :errors))

(def path "data/soccer_match_event_dataset/")
(def get-file #(io/resource (str path %)))
(def json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case))))
(def tags (-> (io/resource "data/meta/meta.edn")
              slurp
              edn/read-string
              :tags))
(def matches-raw
  (-> (get-file (str "matches_" championship ".json"))
      slurp
      json->edn
      hash-by-id))
(def events-raw
  (-> (get-file (str "events_" championship ".json"))
      slurp
      json->edn))
(def events-hash
  (->> events-raw (group-by :match-id)))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  [id-keyword]
  (let [match (-> matches-raw
                  id-keyword)
        reduce-by (fn [prop v] (reduce (partial hash-by prop) (sorted-map) v))
        events-filtered (-> events-hash
                            (get-in [(-> id-keyword name Integer.)]))
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
                    (#(filter (fn [{:keys [wy-id]}]
                                (some? (some players-in-match [wy-id])))
                              %))
                    (#(map (fn [p] (assoc
                                    p
                                    :current-national-team-id
                                    (get-in players-national-teams-hash [(-> p :wy-id str keyword) :team-id]))) %))
                    hash-by-id)]
    {:match (->> match
                 (assoc-names players)
                 (reduce-by :team-id)
                 (#(assoc match :teams-data %)))
     :players-in-match players-in-match
     :players players
     :events events}))

; ==================================
; IO
; ==================================
(if (-> errors some? not)
  (doseq [id-keyword ids-keyword]
    (try
      (let [data (get-data id-keyword)
            id (-> id-keyword name)
            match-label (-> data
                            :match
                            :label
                            (#(clojure.edn/read-string (str "" \" % "\"")))
                            deaccent
                            csk/->snake_case)
            dist "src/main/data/matches/"
            file-extentions [:edn :json]]
        (doseq [file-ext file-extentions]
          (spit
           (str dist (csk/->snake_case championship) "_" match-label "_" id "." (name file-ext))
           ((output-file-type file-ext) data))
          (println (str "Success on spit " dist (csk/->snake_case championship) "_" match-label "_" id "." (name file-ext)))))
      (catch Exception e (println (str "caught exception: " (.getMessage e))))))
  (print errors))
