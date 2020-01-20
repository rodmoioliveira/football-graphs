(ns io.spit-graph-analysis
  (:import [org.jgrapht.graph DefaultWeightedEdge SimpleDirectedWeightedGraph])
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.set :refer [project]]
   [clojure.edn :as edn]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [clojure.set :refer [project]]

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
; (def errors (-> args :errors))

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
    graph))

(println (-> (create-graph team-1) (.getEdgeWeight (-> (create-graph team-1) (.edgeSet) last))))
(println (-> (create-graph team-1) (.outDegreeOf :PTE)))
(println (-> (create-graph team-1) (.inDegreeOf :PTE)))

; (def graph (SimpleDirectedWeightedGraph. DefaultWeightedEdge))
; (doto graph
;   (.addVertex :ZAD)
;   (.addVertex :LAD)
;   (.addVertex :VOE)
;   (.addVertex :LAE)
;   (.addEdge :ZAD :LAD)
;   (.addEdge :VOE :LAE)
;   (.setEdgeWeight :ZAD :LAD 5)
;   (.setEdgeWeight :VOE :LAE 18))

; ==================================
; IO
; ==================================
; (if (-> errors some? not)
;   (let [links (links)
;         graph
;         {:match-id (-> id Integer.)
;          :label (-> data :match :label)
;          :nodes (-> nodes
;                     :nodes
;                     (#(reduce
;                        (fn [acc cur]
;                          (assoc-in
;                           acc
;                           [(-> cur first :current-national-team-id str keyword)]
;                           cur)) {} %)))
;          :links (-> links
;                     (#(reduce
;                        (fn
;                          [acc cur]
;                          (assoc-in
;                           acc
;                           [(-> cur first :team-id str keyword)]
;                           cur)) {} %)))
;          :max-passes (-> links flatten (#(sort-by :value %)) last :value)}
;         match-label (-> data :match :label csk/->snake_case)
;         dist "src/main/data/graphs/"
;         ext (name file-type)]
;     (spit
;      (str dist match-label "." ext)
;      ((output-file-type file-type) graph))
;     (print (str "Success on spit " dist match-label "." ext)))
;   (print errors))
