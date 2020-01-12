(ns io.meta-data
  (:require
   [clojure.data.csv :as csv]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]

   [utils.core :refer [hash-by csv-data->maps output-file-type]]))

; ==================================
; Command Line Options
; ==================================
(def options [["-t" "--type TYPE" "File Type (json or edn)"
               :default :edn
               :parse-fn keyword
               :validate [#(or (= % :edn) (= % :json)) "Must be json or edn"]]])
(def args (-> *command-line-args* (parse-opts options)))
(def file-type (-> args :options :type))
(def errors (-> args :errors))

; ==================================
; IO
; ==================================
(let [path "data/meta/"
      meta ["event.csv" "position.csv" "schemes.csv" "tags.csv"]
      get-file (fn [f] (-> f
                           (#(io/resource (str path %)))
                           slurp
                           csv/read-csv
                           csv-data->maps))
      reduce-by (fn [prop v] (reduce (partial hash-by prop) (sorted-map) v))
      data (-> (map get-file meta))]

  (if (-> errors some? not)
    (let [[event position schemes tags] data
          format-data
          {:sub-events (->> event
                            (map (fn [v] (assoc v
                                                :event (-> v :event Integer.)
                                                :sub-event (-> v :sub-event Integer.))))
                            (reduce-by :sub-event))
           :position (->> position (reduce-by :code))
           :schemes (->> schemes (reduce-by :scheme))
           :tags (->> tags
                      (map (fn [v] (assoc v
                                          :tag (-> v :tag Integer.))))
                      (reduce-by :tag))}]
      (-> format-data
          (#(spit
             (str "src/main/data/meta/meta" "." (name file-type))
             ((output-file-type file-type) %)))))
    (print errors)))
