(ns football.store)

(defonce store (atom {}))

(defn update-store
  [data]
  (swap! store merge {(-> data :match-id str keyword) data}))
