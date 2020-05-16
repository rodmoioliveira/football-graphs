(ns football.draw-field
  (:require
   [football.store :refer [theme-store]]))

(defn draw-background
  "Draw the field background."
  [^js config]
  (let [ctx (-> config :ctx)]
    (doto ctx
      (.clearRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height))
      ((fn [v] (set! (.-fillStyle v) (-> @theme-store :theme-background))))
      (.fillRect 0 0 (-> config :canvas .-width) (-> config :canvas .-height)))))

(defn draw-borders
  "Draw field borders."
  [ctx padding width length]
  (doto ctx
    (.beginPath)
    (.moveTo padding padding)
    (.lineTo padding (- width padding))
    (.lineTo (- length padding) (- width padding))
    (.lineTo (- length padding) padding)
    (.lineTo padding padding)
    (.stroke)))

(defn draw-corners
  "Draw field corners."
  [ctx padding width length corner-radius]
  (doto ctx
    (.beginPath)
    (.arc padding padding corner-radius 0 (/ js/Math.PI 2))
    (.stroke)
    (.beginPath)
    (.arc padding (- width padding) corner-radius (* js/Math.PI 1.5) (* 2 js/Math.PI))
    (.stroke)
    (.beginPath)
    (.arc (- length padding) (- width padding) corner-radius (* 1 js/Math.PI) (* 1.5 js/Math.PI))
    (.stroke)
    (.beginPath)
    (.arc (- length padding) padding corner-radius (* 0.5 js/Math.PI) (* 1 js/Math.PI))
    (.stroke)))

(defn draw-midfield-line
  "Draw midfield line."
  [ctx padding width length flip?]
  (doto ctx
    (.beginPath)
    (#(if flip?
        (doto %
          (.moveTo padding (/ width 2))
          (.lineTo (- length padding) (/ width 2)))
        (doto %
          (.moveTo (/ length 2) padding)
          (.lineTo (/ length 2) (- width padding)))))
    (.stroke)))

(defn draw-midfield-circle
  "Draw midfield circle."
  [ctx width length flip? midfield-cicle-radius]
  (doto ctx
    (.beginPath)
    (.arc
     (/ length 2)
     (/ width 2)
     (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
     0
     (* 2 js/Math.PI))
    (.stroke)))

(defn draw-midfield-point
  "Draw midfield point."
  [ctx width length midfield-point-radius]
  (doto ctx
    (.beginPath)
    (.arc (/ length 2) (/ width 2) midfield-point-radius 0 (* 2 js/Math.PI))
    (.fill)))

(defn draw-goals-rects
  "Draw all gol rect areas."
  [ctx
   {:keys [flip?
           length
           width
           padding
           gol-length
           gol-area-length
           penal-area-length
           penal-area-width
           gol-area-width]}]
  (if flip?
    (doto ctx
      (.save)
      ((fn [v] (set! (.-fillStyle v) (-> @theme-store :theme-background))))
      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 penal-area-length)))
       padding
       (/ width penal-area-length)
       (/ width penal-area-width))
      (.fill)
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect (- (/ length 2) (/ width (* 2 gol-length))) 0 (/ width gol-length) padding)
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 gol-area-length)))
       padding
       (/ width gol-area-length)
       (/ width gol-area-width))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 penal-area-length)))
       (- width padding (/ width penal-area-width))
       (/ width penal-area-length)
       (/ width penal-area-width))
      (.fill)
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect (- (/ length 2) (/ width (* 2 gol-length))) (- width padding) (/ width gol-length) padding)
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- (/ length 2) (/ width (* 2 gol-area-length)))
       (- width padding (/ width gol-area-width))
       (/ width gol-area-length)
       (/ width gol-area-width))
      (.stroke)
      (.restore))

    (doto ctx
      (.save)
      ((fn [v] (set! (.-fillStyle v) (-> @theme-store :theme-background))))
      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       padding
       (- (/ width 2) (/ length (* 2 penal-area-length)))
       (/ length penal-area-width)
       (/ length penal-area-length))
      (.fill)
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect
       0
       (- (/ width 2) (/ length (* 2 gol-length)))
       padding
       (/ length gol-length))
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       padding
       (- (/ width 2) (/ length (* 2 gol-area-length)))
       (/ length gol-area-width)
       (/ length gol-area-length))
      (.stroke)

      ; ==============
      ; penal area
      ; ==============
      (.beginPath)
      (.rect
       (- length padding (/ length penal-area-width))
       (- (/ width 2) (/ length (* 2 penal-area-length)))
       (/ length penal-area-width)
       (/ length penal-area-length))
      (.fill)
      (.stroke)

      ; ==============
      ; gol
      ; ==============
      (.beginPath)
      (.rect
       (- length padding)
       (- (/ width 2) (/ length (* 2 gol-length)))
       padding
       (/ length gol-length))
      (.stroke)

      ; ==============
      ; gol area
      ; ==============
      (.beginPath)
      (.rect
       (- length padding (/ length gol-area-width))
       (- (/ width 2) (/ length (* 2 gol-area-length)))
       (/ length gol-area-width)
       (/ length gol-area-length))
      (.stroke)
      (.restore))))

(defn draw-penal-arcs
  "Draw all penal arcs."
  [ctx
   {:keys [flip?
           length
           width
           midfield-cicle-radius
           midfield-point-radius]}]
  (if flip?
    (doto ctx
      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 8.3)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       0
       (* 2 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 8.3)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 1.136)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       0
       (* 2 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 1.136)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))

    (doto ctx
      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 8.3)
       (/ width 2)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       0
       (* 2 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 8.3)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle area
      ; ==============
      (.beginPath)
      (.arc
       (/ length 1.136)
       (/ width 2)
       (if flip? (/ width midfield-cicle-radius) (/ length midfield-cicle-radius))
       0
       (* 2 js/Math.PI))
      (.stroke)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 1.136)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))))

(defn draw-penal-marks
  "Draw penal marks."
  [ctx
   {:keys [flip?
           length
           width
           midfield-point-radius]}]
  (if flip?
    (doto ctx
      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 8.3)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 2)
       (/ width 1.136)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))

    (doto ctx
      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 8.3)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill)

      ; ==============
      ; penal circle point
      ; ==============
      (.beginPath)
      (.arc
       (/ length 1.136)
       (/ width 2)
       midfield-point-radius
       0
       (* 2 js/Math.PI))
      (.stroke)
      (.fill))))

(defn draw-field
  "Draw soccer field on canvas."
  [dimensions ^js data config]
  (let [[a b] (sort dimensions)
        flip? (-> data .-orientation (#(or (= % "gol-bottom") (= % "gol-top"))))
        [width length] (if flip? [b a] [a b])
        field-data (-> data .-field)
        corner-radius (if flip? (/ width 100) (/ length 100))
        padding 10
        gol-length 16
        gol-area-length 5.7
        midfield-cicle-radius 11.3
        midfield-point-radius 3
        gol-area-width 18
        penal-area-width 6.1
        penal-area-length 2.52]
    (doto (-> config :ctx)
      ((fn [v] (set! (.-strokeStyle v) (-> @theme-store :theme-lines-color))))
      ((fn [v] (set! (.-fillStyle v) (-> @theme-store :theme-lines-color))))
      ((fn [v] (set! (.-lineWidth v) (aget field-data "lines-width"))))

      (draw-borders padding width length)
      (draw-corners padding width length corner-radius)
      (draw-midfield-line padding width length flip?)
      (draw-penal-arcs
       {:flip? flip?
        :length length
        :width width
        :midfield-cicle-radius midfield-cicle-radius
        :midfield-point-radius midfield-point-radius})
      (draw-goals-rects
       {:flip? flip?
        :length length
        :width width
        :padding padding
        :gol-length gol-length
        :gol-area-length gol-area-length
        :penal-area-length penal-area-length
        :penal-area-width penal-area-width
        :gol-area-width gol-area-width})
      (draw-penal-marks
       {:flip? flip?
        :length length
        :width width
        :midfield-point-radius midfield-point-radius})
      (draw-midfield-circle width length flip? midfield-cicle-radius)
      (draw-midfield-point width length midfield-point-radius))))
