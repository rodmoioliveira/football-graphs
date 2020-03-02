(ns utils.dom
  (:require
   [clojure.string :refer [split join]]))

(defn label-dom
  "Create a label for each match."
  [match]
  (let [[label score] (-> match :label (split #","))
        [team1 team2] (-> label (split #"-"))
        [score1 score2] (-> score (split #"-"))
        group-name (-> match :match-info :group-name)
        dateutc (-> match :match-info :dateutc)
        venue (-> match :match-info :venue)
        match-id (-> match :match-id)]
    (str
     "<h2 class='graph__label'
        data-match-id='"
     match-id
     "'>
        <span class='label__team1'>"
     team1
     "</span>
      <span class='label__score1'>"
     score1
     "</span>
        <span class='label__vs'>x</span>
        <span class='label__score2'>"
     score2
     "</span>
        <span class='label__team2'>"
     team2
     "</span>
      </h2>
     <h3 class='graph__info'>"
     (when (not= "" group-name)
       (str
        group-name
        " | "))
     venue
     " | "
     (-> dateutc (split #" ") first (split #"-") ((fn [[y m d]] [d m y])) (#(join "-" %)))
     "</h3>")))

(defn stats-dom
  [algebraic-connectivity global-clustering]
  (str
   "<p class='graph__stats'>
      <span>
      Conectividade:
      </span>
      <span class='graph__metric'>"
   algebraic-connectivity
   "</span>
      <span>
      | Agrupamento:
      </span>
      <span class='graph__metric'>"
   global-clustering
   "</span>
      </p>"))

(defn canvas-dom
  "Create canvas elements."
  [match]
  (let [[label] (-> match :label (split #","))
        match-id (-> match :match-id)
        get-name (fn [id] (-> match :teams-info (#(get-in % [(keyword (str id))])) :name))
        get-ac (fn [id] (-> match :graph-metrics (#(get-in % [(keyword (str id))])) :algebraic-connectivity (.toFixed 1)))
        get-gclus (fn [id] (-> match :graph-metrics (#(get-in % [(keyword (str id))])) :global-clustering-coefficient (.toFixed 3)))
        team1-id (-> match :match-info :home-away :home)
        team2-id (-> match :match-info :home-away :away)
        team1-name (get-name team1-id)
        team2-name (get-name team2-id)
        team1-ac (get-ac team1-id)
        team2-ac (get-ac team2-id)
        team1-gc (get-gclus team1-id)
        team2-gc (get-gclus team2-id)]
    (str
     "<div class='graphs__wrapper'>
      <div class='graph'>
      <p class='graph__confront'>
      <span class='graph__team'>"
     team1-name
     "</span>
      <span>
      vs "
     team2-name
     "</span>
      </p>"
     (stats-dom team1-ac team1-gc)
     "<canvas
        data-match-id='"
     match-id
     "'data-team-id='"
     team1-id
     "'class='graph__canvas'
        id='"
     team1-id
     " - "
     label
     "' data-orientation='gol-left'
        height='720'
        width='1107'></canvas>
      </div>"
     "<div class='graph'>
     <p class='graph__confront'>
      <span class='graph__team'>"
     team2-name
     "</span>
      <span>
      vs "
     team1-name
     "</span>
      </p>"
     (stats-dom team2-ac team2-gc)
     "<canvas
        data-match-id='"
     match-id
     "'data-team-id='"
     team2-id
     "'class='graph__canvas'
        id='"
     team2-id
     " - "
     label
     "' data-orientation='gol-left'
        height='720'
        width='1107'></canvas>
     </div>
     </div>")))

(defn plot-dom
  "Plot graphs in the dom."
  [matches]
  (let [plot-section (-> js/document (.querySelector "[data-plot-graphs]"))]
    (doseq [match matches]
      (-> plot-section (.insertAdjacentHTML "beforeend" (-> ((juxt label-dom canvas-dom) match) (#(join "" %))))))))

(defn reset-dom
  "Reset graphs in the dom."
  []
  (let [plot-section (-> js/document (.querySelector "[data-plot-graphs]"))]
    (-> plot-section (#(set! (.-innerHTML %) "")))))

