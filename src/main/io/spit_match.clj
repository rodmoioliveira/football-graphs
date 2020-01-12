(ns io.spit-match
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   ; [clojure.set :refer [project]]
   [utils.core :refer [hash-by output-file-type assoc-names]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

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

(defn get-data
  []
  (let [path "data/soccer_match_event_dataset/"
        get-file #(io/resource (str path %))
        list->hash (fn [v] (reduce (partial hash-by :wy-id) (sorted-map) v))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        match (-> (get-file "matches_World_Cup.json")
                  slurp
                  json->edn
                  list->hash
                  id-keyword)
        teams-ids (-> match
                      :teams-data
                      vals
                      (#(map (fn [{:keys [team-id]}] team-id) %)))
        reduce-by (fn [prop v] (reduce (partial hash-by prop) (sorted-map) v))
        players (-> (get-file "players.json")
                    slurp
                    json->edn
                    (#(filter (fn [{:keys [current-national-team-id]}]
                                (some (fn [id] (= id current-national-team-id)) teams-ids))
                              %))
                    list->hash)]
    {:match (->> match
                 (assoc-names players)
                 (reduce-by :team-id)
                 (#(assoc match :teams-data %)))

     :players players
     ; :project (-> players
     ;              vals
     ;              (#(map (fn [p] (assoc p :pos :???)) %))
     ;              (project [:pos :wy-id :short-name])
     ;              vec
     ;              list->hash)
     :events (-> (get-file "events_World_Cup.json")
                 slurp
                 json->edn
                 (#(filter (fn [e] (= (-> e :match-id) id)) %)))}))

(if (-> errors some? not)
  (let [data (get-data)
        match-label (-> data :match :label csk/->snake_case)]
    (spit
     (str "src/main/data/matches/" match-label "." (name file-type))
     ((output-file-type file-type) data))
    (print (str "Success on spit " match-label)))
  (print errors))
