(ns utils.core
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pp]
   #?(:clj [clojure.data.json :as json])))

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
  (-> v #?(:cljs clj->js :clj identity) #?(:cljs js/console.log :clj print)) v)

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
      :gol-top (fn [[x y]] [x (- 100 y)])
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
     [canvas-width nodes x y radius]
     (let [rsq (+ (* 5 canvas-width) (* radius radius))
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
