(ns football.store
  (:require
   [utils.dom :refer [is-development?]]
   [football.matches :refer [world-cup-matches-hash]]))

(defonce initial-state (if (is-development?) (world-cup-matches-hash) {}))
(defonce store (atom initial-state))
(defn update-store
  [data]
  (swap! store merge {(-> data :match-id str keyword) data}))

(defonce all-simulations (atom []))
(defn flush-simulations!
  []
  (reset! all-simulations []))
(defn stop-simulations
  []
  (doseq [simulation @all-simulations]
    (-> simulation (.stop))))

(defn make-active-node-store
  []
  (atom nil))
(defn update-active-node-store!
  [store id]
  (reset! store id))
(defn reset-active-node-store!
  [store]
  (reset! store nil))
