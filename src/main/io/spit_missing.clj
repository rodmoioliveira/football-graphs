(ns io.spit-missing
  (:require
   [clojure.set :refer [difference]]
   [clojure.string :as s]
   [clojure.java.io :as io]
   [utils.core :refer [output-file-type]]))

(defn get-files-from
  [dir]
  (->>
   (clojure.java.io/file (str "./src/main/data/" dir "/"))
   file-seq
   (take 100000)
   (map #(-> % (.getName)))
   (filter #(-> % (s/split #"\.") second (= "edn")))
   (remove #(or (= % "filenames.edn") (= % "missing.edn")))
   set))

; ==================================
; IO
; ==================================
(let [data {:missing
            (-> (difference
                 (-> (get-files-from "matches"))
                 (-> (get-files-from "analysis")))
                vec
                sort)}
      dist "src/main/data/analysis/"]
  (spit
   (str dist "missing.edn")
   ((output-file-type :edn) data))
  (println (str "Success on spit " dist "missing.edn")))
