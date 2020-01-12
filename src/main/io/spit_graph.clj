(ns io.spit-graph
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.set :refer [project]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]

   [mapping.matches :refer [map->pos]]
   [mapping.tatical :refer [positions]]
   [utils.core :refer [hash-by output-file-type]]))

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
  (let [path "data/"
        get-file #(io/resource (str path %))
        list->hash (fn [v] (reduce (partial hash-by :wy-id) (sorted-map) v))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        parse (if (= file-type :edn) edn/read-string json->edn)
        filename (->> (get-file "soccer_match_event_dataset/matches_World_Cup.json")
                      slurp
                      json->edn
                      list->hash
                      id-keyword
                      :label
                      csk/->snake_case
                      (#(str % "." (name file-type))))
        data (-> (str path "matches/" filename) io/resource slurp parse)]
    data))

(def data (get-data))
(def team-positions (-> map->pos id-keyword :players))
(def tatical-scheme (-> map->pos id-keyword :tatical))

(defn nodes
  []
  (let [player-position (fn [p] (team-positions (-> p :wy-id str keyword)))
        scheme-position (fn [p] (assoc p
                                       :coord-pos
                                       (-> p
                                           :current-national-team-id
                                           str
                                           keyword
                                           tatical-scheme
                                           positions
                                           ((fn [scheme-pos] (-> p :pos scheme-pos))))))
        aggregate-players (fn [t]
                            (reduce (fn [acc cur]
                                      (assoc-in
                                       acc
                                       [(-> cur :id)]
                                       (assoc cur
                                              :short-name
                                              (conj
                                               (-> cur :id acc :short-name)
                                               (-> cur :short-name)))))
                                    {} t))]
    (-> data
        :players
        vals
        (#(map (fn [p] (assoc p :pos (player-position p) :id (player-position p))) %))
        (#(remove (fn [p] (= (-> p :pos) :???)) %))
        (project [:id :pos :short-name :current-national-team-id])
        vec
        (#(map scheme-position %))
        (#(group-by :current-national-team-id %))
        vals
        (#(map aggregate-players %))
        (#(map vals %)))))

(if (-> errors some? not)
  (let [graph {:nodes (nodes)}
        match-label (-> data :match :label csk/->snake_case)
        dist "src/main/data/graphs/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) graph))
    (print (str "Success on spit " dist match-label "." ext)))
  (print errors))
