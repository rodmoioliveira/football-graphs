(ns reactive.app
  (:require ["rxjs" :as rx :refer [fromEvent]]
            ["rxjs/operators" :as rx-op]))

(defn init []
  (js/console.log "Hello World"))

(def root (-> js/document (.getElementById "root")))

(defn set-div
  [value]
  (-> root
      .-innerHTML
      (set! value)))

(defn x-pos
  [e]
  (-> e
      .-clientY
      (#(str "posição x: " %))))

; https://cljs.github.io/api/cljs.core/DOT
(-> js/document
    (fromEvent "click")
    (.pipe (rx-op/map x-pos) (rx-op/startWith "posição x: ???"))
    (.subscribe set-div))
