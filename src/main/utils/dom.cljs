(ns utils.dom
  (:require
   [clojure.string :refer [split join]]))

(def dom
  {:node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
   :node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
   :coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
   :position-select (-> js/document (.querySelector (str "[data-metric='position']")))
   :min-passes-input (-> js/document (.querySelector (str "[data-metric='min-passes-to-display']")))
   :min-passes-span (-> js/document (.querySelector (str "[data-min-passes-value]")))
   :menu (-> js/document (.querySelector ".nav-menu"))
   :theme-btn (-> js/document (.querySelector "[data-toogle-theme]"))
   :body-theme (-> js/document (.querySelector "[data-theme]"))
   :activate-btn (-> js/document (.querySelector "[data-active-metrics]"))
   :deactivate-btn (-> js/document (.querySelector "[data-deactivate-metrics]"))
   :nav (-> js/document (.querySelector ".nav-metrics"))
   :breakpoint (-> js/document (.querySelector ".sticky-nav-breakpoint"))})

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
  "Create the stats values for each graph."
  [algebraic-connectivity
   global-clustering
   average-node-connectivity]
  (str
   "<p class='graph__stats'>
      <span>
      Coesão:
      </span>
      <span class='graph__metric'>"
   algebraic-connectivity
   "</span>
      <span>
      | Triangulação:
      </span>
      <span class='graph__metric'>"
   global-clustering
   "</span>
      <span>
      | Resistência:
      </span>
      <span class='graph__metric'>"
   average-node-connectivity
   "</span>
      </p>"))

(defn canvas-dom
  "Create canvas elements."
  [match]
  (let [[label] (-> match :label (split #","))
        match-id (-> match :match-id)
        get-name (fn [id] (-> match :teams-info (#(get-in % [(keyword (str id))])) :name))
        get-ac (fn [id] (-> match :graph-metrics (#(get-in % [(keyword (str id))])) :algebraic-connectivity (.toFixed 3)))
        get-anc (fn [id] (-> match :graph-metrics (#(get-in % [(keyword (str id))])) :average-node-connectivity (.toFixed 3)))
        get-gclus (fn [id] (-> match :graph-metrics (#(get-in % [(keyword (str id))])) :global-clustering-coefficient (.toFixed 3)))
        team1-id (-> match :match-info :home-away :home)
        team2-id (-> match :match-info :home-away :away)
        team1-name (get-name team1-id)
        team2-name (get-name team2-id)
        team1-anc (get-anc team1-id)
        team2-anc (get-anc team2-id)
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
     (stats-dom team1-ac team1-gc team1-anc)
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
     (stats-dom team2-ac team2-gc team2-anc)
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

