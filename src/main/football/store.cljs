(ns football.store)

(defonce initial-state {})
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
(defn restart-simulations
  []
  (doseq [simulation @all-simulations]
    (-> simulation (.restart))))

(defn make-active-node-store
  []
  (atom nil))
(defn update-active-node-store!
  [store id]
  (reset! store id))
(defn reset-active-node-store!
  [store]
  (reset! store nil))

(defonce theme-store (atom {}))
(defn update-theme-store!
  [obj]
  (reset! theme-store obj))
