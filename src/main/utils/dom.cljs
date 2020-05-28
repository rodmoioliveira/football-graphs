(ns utils.dom
  (:require
   [cljs.reader :as reader]
   [clojure.edn :as edn]
   [clojure.string :refer [split join trim replace]]))

(def dom
  {:node-color-select (-> js/document (.querySelector (str "[data-metric='node-color']")))
   :node-area-select (-> js/document (.querySelector (str "[data-metric='node-area']")))
   :coverage-select (-> js/document (.querySelector (str "[data-metric='coverage']")))
   :min-passes-input (-> js/document (.querySelector (str "[data-metric='min-passes-to-display']")))
   :compare? (-> js/document (.querySelector (str "[data-metric='compare']")))
   :compare-text (-> js/document (.querySelector ".metric-compare-text"))
   :min-passes-span (-> js/document (.querySelector (str "[data-min-passes-value]")))
   :menu (-> js/document (.querySelector ".nav-menu"))
   :document js/document
   :theme-btn (-> js/document (.querySelector "[data-toogle-theme]"))
   :body-theme (-> js/document (.querySelector "[data-theme]"))
   :activate-btn (-> js/document (.querySelector "[data-active-metrics]"))
   :deactivate-btn (-> js/document (.querySelector "[data-deactivate-metrics]"))
   :nav (-> js/document (.querySelector ".nav-metrics"))
   :plot-section (-> js/document (.getElementById "data-plot-graphs"))
   :matches-lists (-> js/document (.getElementById "matches__lists"))
   :slider-graph (-> js/document (.querySelector ".slider__graph"))
   :slider-home (-> js/document (.querySelector ".slider__home"))
   :slide-to-home (-> js/document (.querySelector "[data-slide-to-home]"))
   :slide-view (-> js/document (.querySelector "[data-view]"))})

(defn toogle-theme-btn
  [theme-text]
  (-> dom :theme-btn ((fn [el] (set! (.-innerHTML el) theme-text)))))

(defn slide-home
  [] (-> dom :slide-view (.setAttribute "data-view" "home")))

(defn slide-graph
  [match-id]
  (-> dom :plot-section (.setAttribute "data-match-id" match-id))
  (-> dom :slide-view (.setAttribute "data-view" "graph")))

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
      (#(-> dom :matches-lists (.querySelector (str "[data-match-id='" % "']"))))
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
  (-> dom :body-theme (.setAttribute "data-theme" theme)))

(defn label-dom
  "Create a label for each match."
  [match]
  (let [[label score] (-> match :label (split #","))
        [team1 team2] (-> label (split #" - "))
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
     (clojure.edn/read-string (str "" \" team1 "\""))
     "</span>
      <span class='label__score1'>"
     score1
     "</span>
        <span class='label__vs'>x</span>
        <span class='label__score2'>"
     (-> score2 trim (split " ") first)
     "</span>
        <span class='label__team2'>"
     (clojure.edn/read-string (str "" \" team2 "\""))
     "</span>
      </h2>
     <h3 class='graph__info'>"
     (when (and (not= "" group-name) (not= nil group-name))
       (str
        group-name
        " | "))
     (when (and (not= "" venue) (not= nil venue))
       (str
        (clojure.edn/read-string (str "" \" venue "\""))
        " | "))
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
        [team1-name team2-name] (-> label (split #" - "))
        match-id (-> match :match-id)
        get-metric (fn [team metric] (-> match :stats team metric (.toFixed 3)))
        team1-id (-> match :match-info :home-away :home)
        team2-id (-> match :match-info :home-away :away)
        team1-anc (get-metric :home :average-node-connectivity)
        team2-anc (get-metric :away :average-node-connectivity)
        team1-ac (get-metric :home :algebraic-connectivity)
        team2-ac (get-metric :away :algebraic-connectivity)
        team1-gc (get-metric :home :global-clustering-coefficient)
        team2-gc (get-metric :away :global-clustering-coefficient)]
    (str
     "<div class='graphs__wrapper'>
      <div class='graph'>
      <p class='graph__confront'>
      <span class='graph__team'>"
     (clojure.edn/read-string (str "" \" team1-name "\""))
     "</span>
      <span>
      vs "
     (clojure.edn/read-string (str "" \" team2-name "\""))
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
     (clojure.edn/read-string (str "" \" team2-name "\""))
     "</span>
      <span>
      vs "
     (clojure.edn/read-string (str "" \" team1-name "\""))
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
   :min-passes-to-display (-> dom :min-passes-input .-value int)
   :compare? (-> dom :compare? .-checked)})

(defn plot-dom
  "Plot graphs in the dom."
  [matches]
  (doseq [match matches]
    (-> dom
        :plot-section
        ((fn [el] (set! (.-innerHTML el) (-> ((juxt label-dom canvas-dom) match) (#(join "" %)))))))))

(defn plot-matches-list
  "Plot list of matches in the dom."
  [el matches]
  (doseq [match matches]
    (-> el (.insertAdjacentHTML "beforeend" (match-item match)))))

(defn set-compare-text!
  [{:keys [compare?]}]
  (-> dom :compare-text (#(set! (.-innerHTML %) (if compare? "(yes)" "(no)")))))

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

(defn set-in-storage!
  [value prop]
  (-> js/localStorage (.setItem prop value)))
