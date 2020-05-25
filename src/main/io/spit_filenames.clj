(ns io.spit-filenames
  (:require
   [clojure.edn :as edn]
   [clojure.string :as s]
   [clojure.java.io :as io]

   [utils.core :refer [output-file-type normalize-filename]]))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  []
  (let [directory (clojure.java.io/file "./src/main/data/analysis/")
        files (file-seq directory)
        path "data/matches/"
        get-file #(io/resource (str path %))
        get-data (fn [f] (->> (get-file f)
                              slurp
                              edn/read-string))
        all-files (->>
                   (take 100000 files)
                   (map #(-> % (.getName)))
                   (filter #(-> % (s/split #"\.") second (= "edn")))
                   (remove #(or (= % "filenames.edn") (= % "missing.edn")))
                   sort
                   (map (fn [f]
                          (let [data (get-data f)]
                            {:championship (-> f
                                               (s/split #"_")
                                               first
                                               (#(cond
                                                   (= % "world") "world_cup"
                                                   (= % "european") "european_championship"
                                                   :else %)))
                             :path (str "../data/analysis/" f)
                             :match-id (-> f (s/split #"\.") first (s/split #"_") last Integer.)
                             :label (-> data :match :label (#(clojure.edn/read-string (str "" \" % "\""))))
                             :dateutc (-> data :match :dateutc)
                             :year (-> data :match :dateutc (s/split #"-") first Integer.)
                             :filename f}))))]

    {:files all-files
     :files-ids-hash
     (->> all-files
          (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur)) {}))
     :files-labels-hash
     (->> all-files
          (reduce (fn [acc cur] (assoc-in acc [(-> cur
                                                   :filename
                                                   normalize-filename
                                                   keyword)] cur)) {}))}))

; ==================================
; IO
; ==================================
(let [data (get-data)
      dist "src/main/data/analysis/"]
  (spit
   (str dist  "filenames.edn")
   ((output-file-type :edn) data))
  (println (str "Success on spit " dist "filenames.edn")))
