(ns football.utils
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pp]
   [clojure.data.json :as json]))

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

#?(:cljs
   (defn assoc-pos
     [canvas team formation tatical-schemes]
     (let [placement (partial place-node canvas)
           coords (fn [p] (-> tatical-schemes formation (get-in [(-> p :pos)])))]
       (map (fn [p]
              (assoc-in
               p
               [:coord_pos]
               (apply placement (coords p)))) team))))

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
     (let [rsq (+ (* 2 canvas-width) (* radius radius))
           nodes-length (-> nodes count dec)]
       (loop [i 0]
         (let [interate? (< i nodes-length)
               node (get nodes i)
               dx (- x (-> node .-coord_pos .-x))
               dy (- y (-> node .-coord_pos .-y))
               dist-sq (+ (* dx dx) (* dy dy))
               node-found? (< dist-sq rsq)]
           (if node-found?
             node
             (when interate? (-> i inc recur))))))))
