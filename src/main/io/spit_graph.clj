; TODO: dry this file...
(ns io.spit-graph
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pp]
   [clojure.edn :as edn]
   [clojure.set :refer [project]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]

   [mapping.matches :refer [map->pos]]
   [mapping.tatical :refer [positions]]
   [utils.core :refer [hash-by output-file-type hash-by-id]]))

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

; (defn remove-cycle
;   "Eventos como [24 -> 24 -> 24 -> 25] se transformam em [24 -> 25]"
;   [coll]
;   (let [index-coll (map #(vector %2 %1) coll (range))]
;     (remove (fn [[i e]] (let [last? (= (+ i 1) (count coll))]
;                           (if last?
;                             false
;                             (= (-> e :playerId) (get-in coll [(+ 1 i) :playerId])))))
;             index-coll)))

(defn link-passes
  "Cria relacionamento de links"
  [teams]
  (map (fn [links]
         (map (fn [link]
                (map (fn [[source target]]
                       {:source (get-in source [:pos])
                        :target (get-in target [:pos])
                        :team-id (get-in source [:team-id])}) link))
              links))
       teams))

(defn remove-reflexivity
  "Remove links [24 -> 24]"
  [teams]
  (map
   #(remove (fn [{:keys [source target]}] (= source target)) %)
   teams))

(defn get-data
  []
  (let [path "data/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        parse (if (= file-type :edn) edn/read-string json->edn)
        filename (->> (get-file "soccer_match_event_dataset/matches_World_Cup.json")
                      slurp
                      json->edn
                      hash-by-id
                      id-keyword
                      :label
                      csk/->snake_case
                      (#(str % "." (name file-type))))
        data (-> (str path "matches/" filename) io/resource slurp parse)]
    data))

(def data (get-data))
(def team-positions (-> map->pos id-keyword :players))
(def tatical-scheme (-> map->pos id-keyword :tatical))

(defn get-nodes
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
                                    {} t))
        players-with-position (-> data
                                  :players
                                  vals
                                  (#(map (fn [p] (assoc p :pos (player-position p) :id (player-position p))) %)))]
    {:players-hash (-> players-with-position hash-by-id)
     :nodes (-> players-with-position
                (#(remove (fn [p] (= (-> p :pos) :???)) %))
                (project [:id :pos :short-name :current-national-team-id])
                vec
                (#(map scheme-position %))
                (#(group-by :current-national-team-id %))
                vals
                (#(map aggregate-players %))
                (#(map vals %)))}))

(def nodes (get-nodes))

(defn links
  []
  (let [assoc-player-data
        #(assoc-in % [:pos]
                   (get-in (-> nodes :players-hash) [(-> % :player-id str keyword) :pos]))]

    (-> data
        :events
        ((fn [p] (map assoc-player-data p)))
        ; ####################################
        ; FIXME: Fix logic of passes
        ; Other events must be consider for passing network...

        ; Rules
        ; 1 - Remove "Ground loose ball duel" if previous teamID is different
        ; 2 - Remove cycles between players of the same team

        ; remove-bad-steal
        ; (#(map second %))
        (#(partition-by :team-id %))
        ((fn [v] (group-by #(-> % first :team-id) v)))
        vals
        ; ((fn [teams] (map (fn [team] (map remove-cycle team)) teams)))
        ; ((fn [teams] (map (fn [team] (map (fn [v] (map second v)) team)) teams)))
        ((fn [teams] (map (fn [team] (map #(partition 2 1 %) team)) teams)))
        link-passes
        ; ; ####################################

        ((fn [teams] (map (fn [team] (flatten team)) teams)))
        ((fn [teams] (map frequencies teams)))
        ((fn [teams] (map (fn [team] (map (fn [[ks v]] (merge ks {:value v})) team)) teams)))
        ((fn [teams] (map #(sort-by :value %) teams)))
        ; ; logger

        ; ; FIXME: this transformation MUST be remove at some point
        remove-reflexivity
        ; passes-count
        )))

(if (-> errors some? not)
  (let [graph {:match-id (-> id Integer.)
               :label (-> data :match :label)
               :nodes (-> nodes
                          :nodes (#(reduce
                                    (fn [acc cur]
                                      (assoc-in
                                       acc
                                       [(-> cur first :current-national-team-id str keyword)]
                                       cur)) {} %)))
               :links (-> (links)
                          (#(reduce (fn
                                      [acc cur]
                                      (assoc-in
                                       acc
                                       [(-> cur first :team-id str keyword)]
                                       cur)) {} %)))}
        match-label (-> data :match :label csk/->snake_case)
        dist "src/main/data/graphs/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) graph))
    (print (str "Success on spit " dist match-label "." ext)))
  (print errors))
