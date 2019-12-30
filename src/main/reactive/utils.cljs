(ns reactive.utils)

(defn get-distance
  [x1 y1 x2 y2]
  (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2) (js/Math.pow (- y2 y1) 2))))

(defn find-point
  [x1 y1 x2 y2 distance1 distance2]
  #js {:x (- x2 (/ (* distance2 (- x2 x1)) distance1))
       :y (- y2 (/ (* distance2 (- y2 y1)) distance1))})

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
  [nodes x y radius]
  (let [rsq (* radius radius)
        nodes-length (-> nodes count dec)]
    (loop [i 0]
      (let [interate? (< i nodes-length)
            node (get nodes i)
            dx (- x (-> node .-initial_pos .-x))
            dy (- y (-> node .-initial_pos .-y))
            dist-sq (+ (* dx dx) (* dy dy))
            node-found? (< dist-sq rsq)]
        (if node-found?
          node
          (if interate? (-> i inc recur)))))))
