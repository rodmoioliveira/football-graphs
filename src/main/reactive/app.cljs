(ns reactive.app
  (:require [reactive.rxjs :refer [move-mouse$]]))

(defn init []
  (move-mouse$))

