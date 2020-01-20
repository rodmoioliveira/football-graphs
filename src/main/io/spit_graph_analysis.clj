(ns io.spit-graph-analysis
  ; https://jgrapht.org/guide/UserOverview#graph-structures
  ; https://jgrapht.org/javadoc/overview-summary.html
  (:import [org.jgrapht.graph DefaultWeightedEdge SimpleDirectedWeightedGraph]
           [org.jgrapht.alg.scoring
            BetweennessCentrality
            ClusteringCoefficient
            AlphaCentrality
            ClosenessCentrality])
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.edn :as edn]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]

   [utils.core :refer [output-file-type hash-by hash-by-id]]))

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
        data (-> (str path "graphs/" filename) io/resource slurp parse)]
    data))

(def data (get-data))
(def teams-ids (-> data
                   :nodes
                   keys
                   (#(map (fn [id] (-> id name Integer.)) %))
                   (#(sort %))
                   (#(map (fn [id] (-> id str keyword)) %))))
(def nodes (-> data
               :nodes
               vals
               (#(sort-by (fn [t] (-> t first :current-national-team-id)) %))))
(def links (-> data
               :links
               vals
               (#(sort-by (fn [t] (-> t first :team-id)) %))))
(def team-1 (-> [nodes links] (#(map first %))))
(def team-2 (-> [nodes links] (#(map second %))))

; ==================================
; Create Graph Data Structure
; ==================================
(defn create-graph
  [team]
  (let [graph (SimpleDirectedWeightedGraph. DefaultWeightedEdge)
        [nodes links] team]
    (doseq [node nodes]
      (doto graph
        (.addVertex (-> node :pos keyword))))
    (doseq [link links]
      (doto graph
        (.addEdge (-> link :source keyword) (-> link :target keyword))))
    (doseq [link links]
      (doto graph
        (.setEdgeWeight (-> link :source keyword) (-> link :target keyword) (-> link :value))))

    (let [vertex-set (-> graph (.vertexSet) vec)
          betweenness-centrality (-> graph (BetweennessCentrality. true) (.getScores))
          clustering-coefficient (-> graph (ClusteringCoefficient.))
          local-clustering-coefficient (-> clustering-coefficient (.getScores))
          average-clustering-coefficient (-> clustering-coefficient (.getAverageClusteringCoefficient))
          closeness-centrality (-> graph (ClosenessCentrality. false true) (.getScores))
          alpha-centrality (-> graph (AlphaCentrality.) (.getScores))
          eigenvector-centrality (-> graph (AlphaCentrality. 0.01	0.0) (.getScores))]
      (-> vertex-set
          (#(map
             (fn [id]
               {:id (name id)
                :metrics {:betweenness-centrality (-> betweenness-centrality id)
                          :local-clustering-coefficient (-> local-clustering-coefficient id)
                          :average-clustering-coefficient average-clustering-coefficient
                          :closeness-centrality (-> closeness-centrality id)
                          :alpha-centrality (-> alpha-centrality id)
                          :eigenvector-centrality (-> eigenvector-centrality id)
                          :degree (-> graph (.degreeOf id))
                          :in-degree (-> graph (.inDegreeOf id))
                          :out-degree (-> graph (.outDegreeOf id))}}) %))
          (#(reduce (partial hash-by :id) (sorted-map) %))))))

(def metrics
  [(create-graph team-1)
   (create-graph team-2)])

; ==================================
; IO
; ==================================
(if (-> errors some? not)
  (let [graph (-> data
                  ((fn [d]
                     (assoc
                      d
                      :nodes
                      {(-> teams-ids first)
                       (-> nodes
                           first
                           (#(map (fn [n] (assoc
                                           n
                                           :metrics
                                           (get-in metrics [0 (-> n :id) :metrics])))
                                  %)))
                       (-> teams-ids second)
                       (-> nodes
                           second
                           (#(map (fn [n] (assoc
                                           n
                                           :metrics
                                           (get-in metrics [1 (-> n :id) :metrics])))
                                  %)))}))))

        match-label (-> data :label csk/->snake_case)
        dist "src/main/data/analysis/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) graph))
    (print (str "Success on spit " dist match-label "." ext)))
  (print errors))
