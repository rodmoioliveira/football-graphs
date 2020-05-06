(ns utils.dom
  (:require
   [cljs.reader :as reader]
   [mapping.themes :refer [theme-identity
                           get-theme-with]]
   [clojure.string :refer [split join trim replace]]))

(def dom
  {:node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
   :node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
   :coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
   :min-passes-input (-> js/document (.querySelector (str "[data-metric='min-passes-to-display']")))
   :min-passes-span (-> js/document (.querySelector (str "[data-min-passes-value]")))
   :menu (-> js/document (.querySelector ".nav-menu"))
   :document js/document
   :theme-btn (-> js/document (.querySelector "[data-toogle-theme]"))
   :body-theme (-> js/document (.querySelector "[data-theme]"))
   :activate-btn (-> js/document (.querySelector "[data-active-metrics]"))
   :deactivate-btn (-> js/document (.querySelector "[data-deactivate-metrics]"))
   :nav (-> js/document (.querySelector ".nav-metrics"))
   :plot-section (-> js/document (.getElementById "data-plot-graphs"))
   :matches-list (-> js/document (.getElementById "matches__list"))
   :slider-graph (-> js/document (.querySelector ".slider__graph"))
   :slider-home (-> js/document (.querySelector ".slider__home"))
   :slide-to-home (-> js/document (.querySelector "[data-slide-to-home]"))
   :slide-view (-> js/document (.querySelector "[data-view]"))})

(defn toogle-theme-btn
  [theme-text]
  (do
    (-> dom :theme-btn ((fn [el] (set! (.-innerHTML el) theme-text))))))

(defn slide-home
  [] (-> dom :slide-view (.setAttribute "data-view" "home")))

(defn slide-graph
  [match-id]
  (do
    (-> dom :plot-section (.setAttribute "data-match-id" match-id))
    (-> dom :slide-view (.setAttribute "data-view" "graph"))))

(defn activate-nav
  [_] (-> dom :nav (.setAttribute "data-active" 1)))

(defn deactivate-nav
  [_] (-> dom :nav (.setAttribute "data-active" 0)))

(defn fix-nav
  [v] (-> dom :menu (.setAttribute "data-sticky" v)))

(defn fix-back
  [v] (-> dom :slide-to-home (.setAttribute "data-sticky" v)))

(defn set-collapse
  [el v]
  (-> el (.setAttribute "data-collapse" v)))

(defn fetch-then
  [url fns]
  (-> js/window
      (.fetch url)
      (.then #(.text %))
      (.then (fn [data] (-> (reader/read-string data)
                            ((fn [v] (doseq [f fns] (f v)))))))))

(def base-url "https://raw.githubusercontent.com/rodmoioliveira/football-graphs/master/src/main/data/analysis/")

(defn fetch-file
  [filename fns]
  (-> (str base-url filename) (fetch-then fns)))

(defn is-mobile?
  []
  (< (-> js/window .-innerWidth) 901))

(defn is-development?
  []
  (-> js/window .-location .-hostname (= "localhost")))

(defn scroll-to-current-match
  []
  (-> dom
      :plot-section
      (.getAttribute "data-match-id")
      (#(-> dom :matches-list (.querySelector (str "[data-match-id='" % "']"))))
      (.scrollIntoView #js {:block "center"})))

(defn scroll-top
  []
  (-> js/window (.scrollTo 0 0)))

(defn is-body-click?
  [e] (->> e
           .-path
           array-seq
           (map #(-> % .-tagName))
           set
           (#(or (contains? % "NAV") (contains? % "BUTTON")))
           not))

(defn toogle-theme
  [theme]
  (do
    (-> dom :body-theme (.setAttribute "data-theme" theme))))

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
       id='graph__label'
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
     (-> score2 trim (split " ") first)
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
      Cohesion:
      </span>
      <span class='graph__metric'>"
   algebraic-connectivity
   "</span>
      <span>
      | Triangulation:
      </span>
      <span class='graph__metric'>"
   global-clustering
   "</span>
      <span>
      | Resistance:
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

(defn match-item
  "Plot a match item in the dom."
  [match]
  (str
   "<li
      class='match-item'
      data-match-id='"
   (-> match :match-id)
   "'>"
   (-> match :label)
   "
   </li>"))

(def loader-element "<div class='loader'></div>")

(defn get-current-theme
  []
  (-> dom :body-theme (.getAttribute "data-theme") keyword))

(defn get-metrics
  []
  {:node-color-metric (-> dom :node-color-select .-value keyword)
   :node-radius-metric (-> dom :node-area-select .-value keyword)
   :min-passes-to-display (-> dom :min-passes-input .-value int)})

(defn plot-dom
  "Plot graphs in the dom."
  [matches]
  (doseq [match matches]
    (-> dom
        :plot-section
        ((fn [el] (set! (.-innerHTML el) (-> ((juxt label-dom canvas-dom) match) (#(join "" %)))))))))

(defn plot-matches-list
  "Plot list of matches in the dom."
  [matches]
  (do
    (-> dom :matches-list (#(set! (.-innerHTML %) "")))
    (doseq [match matches]
      (-> dom :matches-list (.insertAdjacentHTML "beforeend" (match-item match))))))

(defn reset-dom
  "Reset graphs in the dom."
  []
  (-> dom :plot-section (#(set! (.-innerHTML %) ""))))

(defn set-hash!
  [hash]
  (-> js/window .-history (.pushState "" "" (str "#" hash))))

(defn reset-hash!
  []
  (-> js/window .-history (.pushState "" "" " ")))

(defn get-hash
  []
  (-> js/window .-location .-hash (replace #"#" "") keyword))

(defn get-in-storage
  [prop]
  (-> js/localStorage (.getItem prop)))

(defn set-in-storage!
  [value prop]
  (-> js/localStorage (.setItem prop value)))

(defn get-storage-theme
  []
  (let [theme-storage (-> "data-theme" get-in-storage)]
    (when theme-storage
      (do
        (get-theme-with (partial theme-identity (keyword theme-storage)))
        (-> dom :body-theme (.setAttribute "data-theme" theme-storage))
        (toogle-theme-btn theme-storage)))))
