(ns io.spit-match-ids
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]

   [utils.core :refer [output-file-type championships]]))

; ==================================
; Command Line Options
; ==================================
(def options [["-t" "--type TYPE" "File Type (json or edn)"
               :default :edn
               :parse-fn keyword
               :validate [#(or (= % :edn) (= % :json)) "Must be json or edn"]]
              ["-c" "--championship CHAMPIONSHIP" "Championship"
               :parse-fn str
               :validate [#(some? (some #{%} championships))
                          (str "Must be a valid championship " championships)]]])
(def args (-> *command-line-args* (parse-opts options)))
(def file-type (-> args :options :type))
(def championship (-> args :options :championship))
(def errors (-> args :errors))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  []
  (let [path "data/soccer_match_event_dataset/"
        get-file #(io/resource (str path %))
        json->edn #(json/read-str % :key-fn (fn [v] (-> v keyword csk/->kebab-case)))
        match-ids (->> (get-file (str "matches_" championship ".json"))
                       slurp
                       json->edn
                       (map :wy-id))]
    {:match-ids match-ids}))

; ==================================
; IO
; ==================================
(if (-> errors some? not)
  (let [data (get-data)
        label championship
        dist "src/main/data/match_ids/"
        ext (name file-type)]
    (spit
     (str dist "" label "_match_ids." ext)
     ((output-file-type file-type) data))
    (println (str "Success on spit " dist "" label "_match_ids." ext)))
  (print errors))
