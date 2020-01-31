(ns io.spit-graph-analysis
  ; https://jgrapht.org/guide/UserOverview#graph-structures
  ; https://jgrapht.org/javadoc/overview-summary.html
  (:import [org.jgrapht.graph
            DefaultWeightedEdge
            SimpleDirectedWeightedGraph
            SimpleWeightedGraph]
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
  (let [sdwg (SimpleDirectedWeightedGraph. DefaultWeightedEdge)
        swg (SimpleWeightedGraph. DefaultWeightedEdge)
        [nodes links] team]
    (doseq [node nodes]
      (doto sdwg
        (.addVertex (-> node :pos keyword)))
      (doto swg
        (.addVertex (-> node :pos keyword))))
    (doseq [link links]
      (doto sdwg
        (.addEdge (-> link :source keyword) (-> link :target keyword)))
      (doto swg
        (.addEdge (-> link :source keyword) (-> link :target keyword))))
    (doseq [link links]
      (doto sdwg
        (.setEdgeWeight (-> link :source keyword) (-> link :target keyword) (-> link :value))))

    (let [get-edges-weight (fn [edges] (map (fn [e] (-> sdwg (.getEdgeWeight e))) edges))
          sum (fn [v] (apply + v))
          vertex-set (-> sdwg (.vertexSet) vec)
          betweenness-centrality (-> sdwg (BetweennessCentrality. true) (.getScores))
          clustering-coefficient (-> sdwg (ClusteringCoefficient.))
          local-clustering-coefficient (-> clustering-coefficient (.getScores))
          average-clustering-coefficient (-> clustering-coefficient (.getAverageClusteringCoefficient))
          global-clustering-coefficient (-> swg ClusteringCoefficient. (.getGlobalClusteringCoefficient))
          closeness-centrality (-> sdwg (ClosenessCentrality.) (.getScores))
          alpha-centrality (-> sdwg (AlphaCentrality.) (.getScores))
          eigenvector-centrality (-> sdwg (AlphaCentrality. 0.01	0.0) (.getScores))]
      (-> vertex-set
          (#(map
             (fn [id]
               (let [in-degree (-> sdwg
                                   (.incomingEdgesOf id)
                                   get-edges-weight
                                   sum)
                     out-degree (-> sdwg
                                    (.outgoingEdgesOf id)
                                    get-edges-weight
                                    sum)]
                 {:id (name id)
                  :metrics {:in-degree in-degree
                            :out-degree out-degree
                            :degree (-> [in-degree out-degree] sum)
                            :betweenness-centrality (-> betweenness-centrality id)
                            :global-clustering-coefficient global-clustering-coefficient
                            :local-clustering-coefficient (-> local-clustering-coefficient id)
                            :average-clustering-coefficient average-clustering-coefficient
                            :closeness-centrality (-> closeness-centrality id)
                            :alpha-centrality (-> alpha-centrality id)
                            :eigenvector-centrality (-> eigenvector-centrality id)}})) %))
          (#(reduce (partial hash-by :id) (sorted-map) %))))))

(def metrics
  [(create-graph team-1)
   (create-graph team-2)])

(defn get-metrics-ranges
  []
  (let [max-val (fn [m] {:max (reduce max m)})
        min-val (fn [m] {:min (reduce min m)})
        merge-maps (fn [v] (apply merge v))
        get-min-max (fn [v] ((juxt min-val max-val) v))
        metric-range (fn [metric] (fn [v] (-> (map metric v) get-min-max merge-maps)))
        in-degree (metric-range :in-degree)
        out-degree (metric-range :out-degree)
        degree (metric-range :degree)
        betweenness-centrality (metric-range :betweenness-centrality)
        global-clustering-coefficient (metric-range :global-clustering-coefficient)
        local-clustering-coefficient (metric-range :local-clustering-coefficient)
        average-clustering-coefficient (metric-range :average-clustering-coefficient)
        closeness-centrality (metric-range :closeness-centrality)
        alpha-centrality (metric-range :alpha-centrality)
        eigenvector-centrality (metric-range :eigenvector-centrality)]
    (-> metrics
        (#(map vals %))
        flatten
        (#(map :metrics %))
        (#((juxt
            degree
            in-degree
            out-degree
            betweenness-centrality
            local-clustering-coefficient
            closeness-centrality
            alpha-centrality
            eigenvector-centrality
            average-clustering-coefficient
            global-clustering-coefficient)
           %))
        ((fn [[degree
               in-degree
               out-degree
               betweenness-centrality
               local-clustering-coefficient
               closeness-centrality
               alpha-centrality
               eigenvector-centrality
               average-clustering-coefficient
               global-clustering-coefficient]]
           {:degree degree
            :in-degree in-degree
            :out-degree out-degree
            :betweenness-centrality betweenness-centrality
            :local-clustering-coefficient local-clustering-coefficient
            :closeness-centrality closeness-centrality
            :alpha-centrality alpha-centrality
            :eigenvector-centrality eigenvector-centrality
            :average-clustering-coefficient average-clustering-coefficient
            :global-clustering-coefficient global-clustering-coefficient})))))

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
                           (#(map (fn
                                    [n]
                                    (assoc
                                     n
                                     :metrics
                                     (get-in metrics [0 (-> n :id keyword) :metrics])))
                                  %)))
                       (-> teams-ids second)
                       (-> nodes
                           second
                           (#(map (fn
                                    [n]
                                    (assoc
                                     n
                                     :metrics
                                     (get-in metrics [1 (-> n :id keyword) :metrics])))
                                  %)))}
                      :meta (merge (-> data :meta) (get-metrics-ranges))))))
        match-label (-> data :label csk/->snake_case)
        dist "src/main/data/analysis/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) graph))
    (print (str "Success on spit " dist match-label "." ext)))
  (print errors))
