; FIXME:
; Calculating metrics for graph 2057988 2057987
; Execution error (IllegalArgumentException) at org.jgrapht.graph.AbstractGraph/assertVertexExist (AbstractGraph.java:131).
; no such vertex in graph: :107623

; Calculating metrics for graph 2057998
; Execution error (NullPointerException) at org.jgrapht.graph.AbstractGraph/assertVertexExist (AbstractGraph.java:129).
; null

; Calculating metrics for graph 2057997
; Execution error (NullPointerException) at org.jgrapht.graph.AbstractGraph/assertVertexExist (AbstractGraph.java:129).
; null

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
   [libpython-clj.require :refer [require-python]]
   [libpython-clj.python :as py :refer [py. py.. py.-]]

   [utils.core :refer [output-file-type hash-by hash-by-id metric-range]]))

; ==================================
; Python interop code...
; ==================================
(require-python '[networkx :as nx])

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
        mg (nx/MultiGraph)
        mdg (nx/MultiDiGraph)
        [nodes links] team]

    ; ====================================
    ; Nodes
    ; ====================================
    (doseq [node nodes]
      ; networkx
      (doto mg
        (py. add_node (-> node :pos)))
      (doto mdg
        (py. add_node (-> node :pos)))

      ; jgrapht
      (doto sdwg
        (.addVertex (-> node :pos keyword)))
      (doto swg
        (.addVertex (-> node :pos keyword))))

    ; ====================================
    ; Links
    ; ====================================
    (doseq [link links]
      (let [s (-> link :source)
            t (-> link :target)
            source (-> link :source keyword)
            target (-> link :target keyword)
            weight (-> link :value)]

        ; networkx
        (doto mg
          (py. add_edge s t :weight weight))
        (doto mdg
          (py. add_edge s t :weight weight))

      ; jgrapht
        (doto sdwg
          (.addEdge source target)
          (.setEdgeWeight source target weight))
        (doto swg
          (.addEdge source target))))

    (let [get-edges-weight (fn [edges] (map (fn [e] (-> sdwg (.getEdgeWeight e))) edges))
          sum (fn [v] (apply + v))
          vertex-set (-> sdwg (.vertexSet) vec)
          betweenness-centrality (-> sdwg (BetweennessCentrality. true) (.getScores))
          clustering-coefficient (-> sdwg (ClusteringCoefficient.))
          local-clustering-coefficient (-> clustering-coefficient (.getScores))
          average-clustering-coefficient (-> clustering-coefficient (.getAverageClusteringCoefficient))
          global-clustering-coefficient (-> swg ClusteringCoefficient. (.getGlobalClusteringCoefficient))
          ; random-walk betweenness centrality
          current_flow_betweenness_centrality (-> mg
                                                  (nx/current_flow_betweenness_centrality
                                                   :weight "weight"
                                                   :normalized true))
          closeness-centrality (-> sdwg (ClosenessCentrality.) (.getScores))
          alpha-centrality (-> sdwg (AlphaCentrality.) (.getScores))
          katz-centrality (-> sdwg (AlphaCentrality. 0.01 1.0) (.getScores))
          eigenvector-centrality (-> sdwg (AlphaCentrality. 0.01	0.0) (.getScores))]
      {:vertex-set
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
                             :local-clustering-coefficient (-> local-clustering-coefficient id)
                             :closeness-centrality (-> closeness-centrality id)
                             :alpha-centrality (-> alpha-centrality id)
                             :katz-centrality (-> katz-centrality id)
                             :current_flow_betweenness_centrality (-> (name id)
                                                                      current_flow_betweenness_centrality)
                             :eigenvector-centrality (-> eigenvector-centrality id)}})) %))
           (#(reduce (partial hash-by :id) (sorted-map) %)))
       :graph-metrics
       {:algebraic-connectivity (-> mg (nx/algebraic_connectivity :weight "weight"))
        :average-node-connectivity (-> mdg (nx/average_node_connectivity))
        :average-clustering-coefficient average-clustering-coefficient
        :global-clustering-coefficient global-clustering-coefficient}})))

(def metrics
  [(create-graph team-1)
   (create-graph team-2)])

(defn get-metrics-ranges
  []
  (let [in-degree (metric-range :in-degree)
        out-degree (metric-range :out-degree)
        degree (metric-range :degree)
        betweenness-centrality (metric-range :betweenness-centrality)
        current_flow_betweenness_centrality (metric-range :current_flow_betweenness_centrality)
        local-clustering-coefficient (metric-range :local-clustering-coefficient)
        closeness-centrality (metric-range :closeness-centrality)
        alpha-centrality (metric-range :alpha-centrality)
        katz-centrality (metric-range :katz-centrality)
        eigenvector-centrality (metric-range :eigenvector-centrality)]
    (-> metrics
        (#(map :vertex-set %))
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
            katz-centrality
            current_flow_betweenness_centrality)
           %))
        ((fn [[degree
               in-degree
               out-degree
               betweenness-centrality
               local-clustering-coefficient
               closeness-centrality
               alpha-centrality
               eigenvector-centrality
               katz-centrality
               current_flow_betweenness_centrality]]
           {:degree degree
            :in-degree in-degree
            :out-degree out-degree
            :betweenness-centrality betweenness-centrality
            :local-clustering-coefficient local-clustering-coefficient
            :closeness-centrality closeness-centrality
            :alpha-centrality alpha-centrality
            :eigenvector-centrality eigenvector-centrality
            :katz-centrality katz-centrality
            :current_flow_betweenness_centrality current_flow_betweenness_centrality})))))

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
                                     (get-in metrics [0 :vertex-set (-> n :id keyword) :metrics])))
                                  %)))
                       (-> teams-ids second)
                       (-> nodes
                           second
                           (#(map (fn
                                    [n]
                                    (assoc
                                     n
                                     :metrics
                                     (get-in metrics [1 :vertex-set (-> n :id keyword) :metrics])))
                                  %)))}
                      :min-max-values (merge (-> data :min-max-values) (get-metrics-ranges))
                      :graph-metrics
                      {(-> teams-ids first) (get-in metrics [0 :graph-metrics])
                       (-> teams-ids second) (get-in metrics [1 :graph-metrics])}))))
        match-label (-> data :label csk/->snake_case)
        dist "src/main/data/analysis/"
        ext (name file-type)]
    (spit
     (str dist match-label "." ext)
     ((output-file-type file-type) graph))
    (print (str "Success on spit " dist match-label "." ext))
    (System/exit 0))
  (print errors))
