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

(defn dot-product [[x1 y1] [x2 y2]] (+ (* x1 x2) (* y1 y2)))

(defn radians->deegres [rad] (/ (* rad 180) js/Math.PI))

; https://www.wikihow.com/Find-the-Angle-Between-Two-Vectors
(defn radians-between
  [vector1 vector2]
  (->
    (/ (dot-product vector1 vector2) (* (vector-length vector1) (vector-length vector2)))
    js/Math.acos))


