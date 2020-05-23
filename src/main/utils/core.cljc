(ns utils.core
  #?(:clj
     (:import java.text.Normalizer
              java.text.Normalizer$Form))
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [project-specs :as pspecs]
   #?(:clj [clojure.spec.test.alpha :as stest]
      :cljs [cljs.spec.test.alpha :as stest])
   #?(:clj [clojure.spec.alpha :as s]
      :cljs [cljs.spec.alpha :as s])
   #?(:clj [clojure.data.json :as json])))

(def championships
  ["England"
   "European_Championship"
   "France"
   "Germany"
   "Italy"
   "Spain"
   "World_Cup"])

#?(:clj
   (defn deaccent
     [str]
     (let [normalized (java.text.Normalizer/normalize str java.text.Normalizer$Form/NFD)]
       (str/replace normalized #"\p{InCombiningDiacriticalMarks}+" ""))))

(defn normalize-filename
  [s]
  (-> s
      (str/split #"\.")
      first
      (str/replace #"\(_p\)_" "")
      (str/replace #"\(_e\)_" "")
      (str/replace #"," "")))

#?(:clj (defn max-val [m] {:max (reduce max m)}))
#?(:cljs (defn max-val [m] (reduce max m)))
#?(:clj
   (s/fdef max-val
     :args ::pspecs/max-val-args
     :ret ::pspecs/max-val-ret))
#?(:cljs
   (s/fdef max-val
     :args ::pspecs/ranges
     :ret ::pspecs/range))
(stest/instrument `max-val)

#?(:clj (defn min-val [m] {:min (reduce min m)}))
#?(:cljs (defn min-val [m] (reduce min m)))
#?(:clj
   (s/fdef min-val
     :args ::pspecs/min-val-args
     :ret ::pspecs/min-val-ret))
#?(:cljs
   (s/fdef min-val
     :args ::pspecs/ranges
     :ret ::pspecs/range))
(stest/instrument `min-val)

(defn merge-maps [v] (apply merge v))
(defn get-min-max [v] ((juxt min-val max-val) v))
(defn metric-range [metric] (fn [v] (-> (map metric v) get-min-max merge-maps)))

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
   (defn hash-by-name [v] (reduce (partial hash-by :name) (sorted-map) v)))

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

(def field-dimensions [123 80])
(defn canvas-dimensions
  [scale]
  (-> field-dimensions (#(map (partial * scale) %))))

#?(:cljs
   (defn set-canvas-dimensions
     [scale]
     {:gol-bottom (fn [c]
                    (set! (.-height c) (-> (canvas-dimensions scale) first))
                    (set! (.-width c) (-> (canvas-dimensions scale) second)))
      :gol-top (fn [c]
                 (set! (.-height c) (-> (canvas-dimensions scale) first))
                 (set! (.-width c) (-> (canvas-dimensions scale) second)))
      :gol-left (fn [c]
                  (set! (.-height c) (-> (canvas-dimensions scale) second))
                  (set! (.-width c) (-> (canvas-dimensions scale) first)))
      :gol-right (fn [c]
                   (set! (.-height c) (-> (canvas-dimensions scale) second))
                   (set! (.-width c) (-> (canvas-dimensions scale) first)))}))

#?(:cljs
   (def mobile-mapping
     {:gol-right :gol-left
      :gol-left :gol-bottom
      :gol-bottom :gol-left
      :gol-top :gol-left}))

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
              (let [coord (-> n :average-pos)
                    pos ((-> coord-mapping orientation) coord)]
                (assoc-in
                 n
                 [:coord]
                 (apply placement pos)))) nodes))))

#?(:cljs
   (defn get-distance
     [x1 y1 x2 y2]
     (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2) (js/Math.pow (- y2 y1) 2)))))
(s/fdef get-distance
  :args (s/coll-of ::pspecs/int-or-double)
  :ret ::pspecs/int-or-double)
(stest/instrument `get-distance)

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
               dx (- x (-> node (aget "coord") .-x))
               dy (- y (-> node (aget "coord") .-y))
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
