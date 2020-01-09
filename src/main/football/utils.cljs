(ns football.utils)

(defn place-node
  [canvas x-% y-%]
  {:x (* (-> canvas .-width) (/ x-% 100))
   :y (* (-> canvas .-height) (/ y-% 100))})

(defn logger [v]
  (-> v clj->js js/console.log)
  v)

(defn assoc-pos
  [canvas team formation tatical-schemes]
  (let [placement (partial place-node canvas)
        coords (fn [p] (-> tatical-schemes formation (get-in [(-> p :pos)])))]
    (map (fn [p]
           (assoc-in
            p
            [:coord_pos]
            (apply placement (coords p)))) team)))

(defn get-distance
  [x1 y1 x2 y2]
  (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2) (js/Math.pow (- y2 y1) 2))))

(defn hash-by
  "Hashmap a collection by a given key"
  [key acc cur]
  (assoc acc (key cur) cur))

(defn vector-length
  "||u|| = √(u1 + u2)"
  [[x y]]
  (js/Math.sqrt (+ (js/Math.pow x 2) (js/Math.pow y 2))))

(defn dot-product
  [[x1 y1] [x2 y2]]
  (+ (* x1 x2) (* y1 y2)))

(defn radians-between
  "https://www.wikihow.com/Find-the-Angle-Between-Two-Vectors"
  [vector1 vector2]
  (->
   (/ (dot-product vector1 vector2) (* (vector-length vector1) (vector-length vector2)))
   js/Math.acos))

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
          (when interate? (-> i inc recur)))))))
