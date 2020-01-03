(ns football.rxjs
  (:require ["rxjs" :as rx]
            ["rxjs/operators" :as rx-op]))

(def root (-> js/document (.getElementById "root")))

(defn set-div
  [value]
  (-> root
      .-innerHTML
      (set! value)))

(defn get-coods
  [e] {:y (.-clientY e) :x (.-clientX e)})

(defn format-str
  [{:keys [x y]}]
  (str "posição x: " x ", posição y: " y))

(defn render-pos
  [e]
  (-> e
      get-coods
      format-str))

; https://cljs.github.io/api/cljs.core/DOT
(defn move-mouse$
  []
  (-> js/document
      (rx/fromEvent "mousemove")
      (.pipe (rx-op/map render-pos) (rx-op/startWith "posição x: ???, posição y: ???"))
      (.subscribe set-div)))
