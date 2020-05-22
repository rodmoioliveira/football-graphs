(ns io.spit-filenames
  (:require
   [clojure.edn :as edn]
   [clojure.string :refer [split replace]]
   [clojure.java.io :as io]

   [utils.core :refer [output-file-type normalize-filename]]))

; ==================================
; Fetch Data
; ==================================
(defn get-data
  []
  (let [directory (clojure.java.io/file "./src/main/data/matches/")
        files (file-seq directory)
        path "data/matches/"
        get-file #(io/resource (str path %))
        get-data (fn [f] (->> (get-file f)
                              slurp
                              edn/read-string))
        all-files (->>
                   (take 1000 files)
                   (map #(-> % (.getName)))
                   (filter #(-> % (split #"\.") second (= "edn")))
                   sort
                   (map (fn [f]
                          (let [data (get-data f)]
                            {:championship (-> f  (split #"_") first (#(if (= % "world") "world_cup" %)))
                             :path (str "../data/analysis/" f)
                             :match-id (-> f (split #"\.") first (split #"_") last Integer.)
                             :label (-> data :match :label)
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
   ((output-file-type :edn) data)))