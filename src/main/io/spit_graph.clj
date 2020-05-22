(ns io.spit-graph
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.set :refer [project]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.string :as s]
   [clojure.pprint :refer [pprint]]

   [utils.core :refer [output-file-type
                       hash-by-id
                       hash-by-name
                       hash-by
                       metric-range
                       championships]]))

; ==================================
; Utils
; ==================================
(defn passes-count
  [v]
  (-> (map (fn [x] (apply + (map (fn [y] (y :value)) x))) v) print)
  v)

; FIXME: refine counts of passes
(defn just-passes
  [e]
  (= (-> e :event-id) 8))

(defn get-event-positions
  [teams]
  (map (fn [links]
         (map (fn [link]
                (map (fn [[source target]]
                       {:source (get-in source [:pos])
                        :target (get-in target [:pos])
                        :source-position (-> source (#(get-in % [:positions])) first)
                        :target-position (-> source (#(get-in % [:positions])) second)
                        :team-id (get-in source [:team-id])}) link))
              links))
       teams))

(defn link-passes
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
  [teams]
  (map
   #(remove (fn [{:keys [source target]}] (= source target)) %)
   teams))

(defn aggregate-positions
  [teams]
  (map
   (fn [team]
     {:team-id (-> team first :team-id)
      :positions (reduce
                  (fn [acc cur]
                    (assoc
                     acc
                     (-> cur :source) (concat (-> acc (#(get-in % [(-> cur :source)]))) [(-> cur :source-position)])
                     (-> cur :target) (concat (-> acc (#(get-in % [(-> cur :target)]))) [(-> cur :target-position)])))
                  {}
                  team)})
   teams))

; ==================================
; Command Line Options
; ==================================
(def options [["-i" "--id ID" "Match ID"]
              ["-t" "--type TYPE" "File Type (json or edn)"
               :default :edn
               :parse-fn keyword
               :validate [#(or (= % :edn) (= % :json)) "Must be json or edn"]]
              ["-c" "--championship CHAMPIONSHIP" "Championship"
               :parse-fn str
               :validate [#(some? (some #{%} championships))
                          (str "Must be a valid championship " championships)]]])
(def args (-> *command-line-args* (parse-opts options)))
(def id (-> args :options :id edn/read-string))
(def id-keyword (-> id str keyword))
(def file-type (-> args :options :type))
(def championship (-> args :options :championship))
(def errors (-> args :errors))

; ==================================
; Test
; ==================================
(defn logger-file [v]
  (spit
   (str "src/main/data/graphs/test.edn")
   ((output-file-type file-type) v))
  v)

(defn get-teams-id-hashmap
  []
  (let [path "data/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        data (->> (get-file "soccer_match_event_dataset/teams.json")
                  slurp
                  json->edn
                  hash-by-id)]
    data))

(defn get-teams-name-hashmap
  []
  (let [path "data/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        data (->> (get-file "soccer_match_event_dataset/teams.json")
                  slurp
                  json->edn
                  hash-by-name)]
    data))

(def teams-id-hashmap (get-teams-id-hashmap))
(def teams-name-hashmap (get-teams-name-hashmap))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  []
  (let [path "data/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        parse (if (= file-type :edn) edn/read-string json->edn)
        filename (->> (get-file (str "soccer_match_event_dataset/matches_" championship ".json"))
                      slurp
                      json->edn
                      hash-by-id
                      id-keyword
                      :label
                      csk/->snake_case
                      (#(str (csk/->snake_case championship) "_" % "_" id "." (name file-type))))
        data (-> (str path "matches/" filename) io/resource slurp parse)]
    data))

(def data (get-data))

; ==================================
; Formatting Data
; ==================================
(defn get-nodes
  []
  (let [pair-subs (fn [t] (mapv
                           (fn [s] (-> [(-> s :player-in) (-> s :player-out)] sort vec)) t))
        formation (->> data
                       :match
                       :teams-data
                       vals
                       (map :formation))
        lineup (->> data :players-in-match vec)
        substitutions (->> formation
                           (map (fn [t] (->> t :substitutions
                                             pair-subs
                                             ((fn [pair-ids]
                                                (vec
                                                 (set
                                                  (map (fn [[id1 id2]]
                                                         (vec
                                                          (set
                                                           (reduce
                                                            concat
                                                            (concat
                                                             (filter (fn [ids] (some (fn [id] (= id id1)) ids)) pair-ids)
                                                             (filter (fn [ids] (some (fn [id] (= id id2)) ids)) pair-ids))))))
                                                       pair-ids))))))))
                           (reduce concat))
        players-in-game (set (concat lineup (->> substitutions (reduce concat))))
        aggregate-players (fn [t]
                            (reduce (fn [acc cur]
                                      (assoc-in
                                       acc
                                       [(-> cur :id)]
                                       (assoc cur
                                              :short-name
                                              (conj
                                               (-> cur :id acc :short-name)
                                               (-> cur
                                                   :short-name
                                                   (#(clojure.edn/read-string (str "" \" % "\""))))))))
                                    {} t))
        players-with-position (->> data
                                   :players
                                   vals
                                   (map (fn [p] (assoc p :pos [(p :wy-id)] :id [(p :wy-id)])))
                                   (map (fn [p]
                                          (let [compose-pos (->> substitutions
                                                                 (filter
                                                                  (fn [ids] (some #(= % (-> p :wy-id)) ids)))
                                                                 first)
                                                pos (or compose-pos [(p :wy-id)])]
                                            (assoc p :pos (s/join "" pos) :id (s/join "" pos))))))]

    {:players-hash (-> players-with-position hash-by-id)
     :substitutions substitutions
     :nodes (-> players-with-position
                (#(filter (fn [{:keys [wy-id]}] (some (set [wy-id]) players-in-game)) %))
                (project [:id :pos :short-name :current-national-team-id])
                vec
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
        ; FIXME: refine counts of passes
        (#(filter just-passes %))
        ((fn [p] (map assoc-player-data p)))
        (#(partition-by :team-id %))
        ((fn [v] (group-by #(-> % first :team-id) v)))
        vals
        ((fn [teams] (map (fn [team] (map #(partition 2 1 %) team)) teams)))
        link-passes
        ((fn [teams] (map (fn [team] (flatten team)) teams)))
        ((fn [teams] (map frequencies teams)))
        ((fn [teams] (map (fn [team] (map (fn [[ks v]] (merge ks {:value v})) team)) teams)))
        ((fn [teams] (map #(sort-by :value %) teams)))
        ; FIXME: this transformation MUST be remove at some point
        remove-reflexivity
        ; logger-file
        ; passes-count
        )))

(defn get-average-pos
  []
  (let [assoc-player-data
        #(assoc-in % [:pos]
                   (get-in (-> nodes :players-hash) [(-> % :player-id str keyword) :pos]))]
    (-> data
        :events
        (#(filter just-passes %))
        ((fn [p] (map assoc-player-data p)))
        (#(partition-by :team-id %))
        ((fn [v] (group-by #(-> % first :team-id) v)))
        vals
        ((fn [teams] (map (fn [team] (map #(partition 2 1 %) team)) teams)))
        get-event-positions
        ((fn [teams] (map (fn [team] (flatten team)) teams)))
        ((fn [teams] (map (fn [team] (filter (fn [e] (->> e vals (every? some?))) team)) teams)))
        ; FIXME: this transformation MUST be remove at some point
        remove-reflexivity
        aggregate-positions
        (#(map (fn [{:keys [team-id positions]}]
                 {:team-id team-id
                  :positions
                  (reduce
                   (partial hash-by :role)
                   (sorted-map)
                   (map (fn
                          [[key all-pos]]
                          (let [sum-pos (fn [pos] (fn [v] (->> (map pos v) (apply +))))]
                            {:role key
                             :med-position
                             ; ====================================
                             ; About x and y positions in this dataset:
                             ; ====================================

                             ; The origin and destination positions associated with the event.
                             ; Each position is a pair of coordinates (x, y). The x and y coordinates are
                             ; always in the range [0, 100] and indicate the percentage of the field from
                             ; the perspective of the attacking team. In particular, the value of the x
                             ; coordinate indicates the event’s nearness (in percentage) to the opponent’s
                             ; goal, while the value of the y coordinates indicates the event’s nearness
                             ; (in percentage) to the right side of the field;

                             ; https://www.nature.com/articles/s41597-019-0247-7
                             ; ====================================
                             (-> ((juxt (sum-pos :x) (sum-pos :y) count) all-pos)
                                 ((fn [[x y c]] [(/ x c) (/ y c)]))
                                 ((fn [v] (map float v)))
                                 ((fn [[x y]] [y (- 100 x)])))}))
                        positions))})
               %))
        (#(reduce (partial hash-by :team-id) (sorted-map) %)))))

; ==================================
; IO
; ==================================
(if (-> errors some? not)
  (let [links (links)
        average-pos (get-average-pos)
        [teams-str] (-> data :match :label (s/split #","))
        [team1 team2] (-> teams-str (s/split #"-") (#(map s/trim %)) (#(map keyword %)))
        edges (-> links
                  (#(reduce
                     (fn
                       [acc cur]
                       (assoc-in
                        acc
                        [(-> cur first :team-id str keyword)]
                        cur)) {} %)))
        graph
        {:match-id (-> id Integer.)
         :label (-> data :match :label)
         :teams-info (-> edges
                         keys
                         ((fn [ks] (map (fn [k] (-> teams-id-hashmap k)) ks)))
                         hash-by-id)
         :match-info
         {:winner (-> data :match :winner)
          :competition-id (-> data :match :competition-id)
          :home-away {:home (-> teams-name-hashmap team1 :wy-id)
                      :away (-> teams-name-hashmap team2 :wy-id)}
          :gameweek (-> data :match :gameweek)
          :duration (-> data :match :duration)
          :season-id (-> data :match :season-id)
          :round-id (-> data :match :round-id)
          :group-name (-> data :match :group-name)
          :status (-> data :match :status)
          :venue (-> data :match :venue)
          :dateutc (-> data :match :dateutc)}
         :nodes (-> nodes
                    :nodes
                    (#(map (fn
                             [team]
                             (map
                              (fn [node] (merge
                                          node
                                          {:average-pos
                                           (-> average-pos
                                               (get-in [(-> node :current-national-team-id str keyword)
                                                        :positions
                                                        (-> node :pos str keyword)
                                                        :med-position]))}))
                              team)) %))
                    (#(reduce
                       (fn [acc cur]
                         (assoc-in
                          acc
                          [(-> cur first :current-national-team-id str keyword)]
                          cur)) {} %)))
         :links edges
         :min-max-values
         {:passes (-> links
                      flatten
                      ((fn [v]
                         (let [value (metric-range :value)]
                           ((juxt value) v))))
                      first)}}

        match-label (-> data :match :label csk/->snake_case)
        dist "src/main/data/graphs/"
        ext (name file-type)]
    (spit
     (str dist (csk/->snake_case championship) "_" match-label "_" id "." ext)
     ((output-file-type file-type) graph))
    (println (str "Success on spit " dist (csk/->snake_case championship) "_" match-label "_" id "." ext)))
  (print errors))
