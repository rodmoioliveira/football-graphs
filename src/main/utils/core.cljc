(ns utils.core
  (:require
   [clojure.string :refer [split]]
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pp]
   #?(:clj [clojure.data.json :as json])))

#?(:cljs
   (defn write-label
     [canvas]
     (let [[label score] (-> canvas :data :label (split #","))
           [team1 team2] (-> label (split #"-"))
           [score1 score2] (-> score (split #"-"))
           label-html (-> js/document (.querySelector
                                       (str
                                        "[data-match-id="
                                        "'"
                                        (-> canvas :data :match-id)
                                        "'"
                                        "].graph__label")))]
       (-> label-html
           (#(set! (.-innerHTML %) (str
                                    "<span class='label__team1'>" team1 "</span>"
                                    "<span class='label__score1'>" score1 "</span>"
                                    "<span class='label__vs'>x</span>"
                                    "<span class='label__score2'>" score2 "</span>"
                                    "<span class='label__team2'>" team2 "</span>")))))))

#?(:cljs
   (defn get-global-metrics
     [matches]
     (let [max-val (fn [m] (reduce max m))
           min-val (fn [m] (reduce min m))
           merge-maps (fn [v] (apply merge v))
           get-min-max (fn [v] ((juxt min-val max-val) v))
           metric-range (fn [metric] (fn [v] (-> (map metric v) get-min-max merge-maps)))
           in-degree (metric-range :in-degree)
           out-degree (metric-range :out-degree)
           degree (metric-range :degree)
           katz-centrality (metric-range :katz-centrality)
           passes (metric-range :passes)
           betweenness-centrality (metric-range :betweenness-centrality)
           global-clustering-coefficient (metric-range :global-clustering-coefficient)
           local-clustering-coefficient (metric-range :local-clustering-coefficient)
           average-clustering-coefficient (metric-range :average-clustering-coefficient)
           closeness-centrality (metric-range :closeness-centrality)
           alpha-centrality (metric-range :alpha-centrality)
           eigenvector-centrality (metric-range :eigenvector-centrality)]

       (-> matches
           (#(map (fn [v] (get-in v [:meta])) %))
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
               global-clustering-coefficient
               passes
               katz-centrality)
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
                  global-clustering-coefficient
                  passes
                  katz-centrality]]
              {:degree degree
               :in-degree in-degree
               :out-degree out-degree
               :betweenness-centrality betweenness-centrality
               :local-clustering-coefficient local-clustering-coefficient
               :closeness-centrality closeness-centrality
               :alpha-centrality alpha-centrality
               :eigenvector-centrality eigenvector-centrality
               :average-clustering-coefficient average-clustering-coefficient
               :global-clustering-coefficient global-clustering-coefficient
               :passes passes
               :katz-centrality katz-centrality}))))))

#?(:clj
   (defn hash-by
     "Hashmap a collection by a given key"
     [key acc cur]
     (assoc acc (-> cur key str keyword) cur)))

#?(:cljs
   (defn hash-by
     "Hashmap a collection by a given key"
     [key acc cur]
     (assoc acc (key cur) cur)))

#?(:clj
   (defn hash-by-id [v] (reduce (partial hash-by :wy-id) (sorted-map) v)))

#?(:clj
   (def output-file-type
     {:edn #(-> % pp/pprint with-out-str)
      :json #(-> % (json/write-str :key-fn (fn [k] (-> k name str csk/->camelCase))))}))

#?(:clj
   (defn csv-data->maps [csv-data]
     (map zipmap
          (->> (first csv-data)
               (map #(-> % csk/->kebab-case keyword))
               repeat)
          (rest csv-data))))

(defn logger [v]
  (-> v #?(:cljs clj->js :clj identity) #?(:cljs js/console.log :clj pp/pprint)) v)

#?(:cljs
   (defn place-node
     [canvas x-% y-%]
     {:x (* (-> canvas .-width) (/ x-% 100))
      :y (* (-> canvas .-height) (/ y-% 100))}))

(def canvas-dimensions
  [950 730])

#?(:cljs
   (def set-canvas-dimensions
     {:gol-bottom (fn [c] (do
                            (set! (.-height c) (-> canvas-dimensions first))
                            (set! (.-width c) (-> canvas-dimensions second))))
      :gol-top (fn [c] (do
                         (set! (.-height c) (-> canvas-dimensions first))
                         (set! (.-width c) (-> canvas-dimensions second))))
      :gol-left (fn [c] (do
                          (set! (.-height c) (-> canvas-dimensions second))
                          (set! (.-width c) (-> canvas-dimensions first))))
      :gol-right (fn [c] (do
                           (set! (.-height c) (-> canvas-dimensions second))
                           (set! (.-width c) (-> canvas-dimensions first))))}))

#?(:cljs
   (def mobile-mapping
     {:gol-right :gol-bottom
      :gol-left :gol-top}))

#?(:cljs
   (def coord-mapping
     {:gol-bottom identity
      :gol-top (fn [[x y]] [(- 100 x) (- 100 y)])
      :gol-left (fn [[x y]] [(- 100 y) x])
      :gol-right (fn [[x y]] [y (- 100 x)])}))

#?(:cljs
   (defn assoc-pos
     [nodes canvas orientation]
     (let [placement (partial place-node canvas)]
       (map (fn [n]
              (let [coord (-> n :coord-pos)
                    pos ((-> coord-mapping orientation) coord)]
                (assoc-in
                 n
                 [:coord]
                 (apply placement pos)))) nodes))))

#?(:cljs
   (defn get-distance
     [x1 y1 x2 y2]
     (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2) (js/Math.pow (- y2 y1) 2)))))

#?(:cljs
   (defn vector-length
     "||u|| = √(u1 + u2)"
     [[x y]]
     (js/Math.sqrt (+ (js/Math.pow x 2) (js/Math.pow y 2)))))

#?(:cljs
   (defn dot-product
     [[x1 y1] [x2 y2]]
     (+ (* x1 x2) (* y1 y2))))

#?(:cljs
   (defn radians-between
     "https://www.wikihow.com/Find-the-Angle-Between-Two-Vectors"
     [vector1 vector2]
     (->
      (/ (dot-product vector1 vector2) (* (vector-length vector1) (vector-length vector2)))
      js/Math.acos)))

#?(:cljs
   (defn find-node
     [config canvas-width nodes x y]
     (let [rsq (* (-> config :nodes :radius-click) canvas-width)
           nodes-length (-> nodes count dec)]
       (loop [i 0]
         (let [interate? (< i nodes-length)
               node (get nodes i)
               dx (- x (-> node .-coord .-x))
               dy (- y (-> node .-coord .-y))
               dist-sq (+ (* dx dx) (* dy dy))
               node-found? (< dist-sq rsq)]
           (if node-found?
             node
             (when interate? (-> i inc recur))))))))

#?(:clj
   (defn assoc-names
     [players match]
     (let [short-name (fn [p]
                        (assoc
                         p
                         :player-name
                         (-> p :player-id str keyword players :short-name)))
           get-sub-names (fn [p]
                           (assoc
                            p
                            :player-in-name
                            (-> p :player-in str keyword players :short-name)
                            :player-out-name
                            (-> p :player-out str keyword players :short-name)))
           get-names (fn [fnc location team]
                       (->> team
                            :formation
                            location
                            (map fnc)))]
       (->> match
            :teams-data
            vals
            (map (fn [team]
                   (assoc
                    team
                    :formation
                    {:bench
                     (->> team (get-names short-name :bench))
                     :lineup
                     (->> team (get-names short-name :lineup))
                     :substitutions
                     (->> team (get-names get-sub-names :substitutions))})))))))
