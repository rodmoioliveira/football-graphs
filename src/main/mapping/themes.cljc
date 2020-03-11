(ns mapping.themes)

(def theme-mapping
  {:light {:reverse :dark
           :theme "dark"
           :text "☀️"
           :background "#121010"
           :font-color "white"
           :lines-color "#333"}
   :dark {:reverse :light
          :theme "light"
          :text "🌛"
          :background "white"
          :font-color "black"
          :lines-color "#ccc"}})

(defn theme-reverse
  [theme prop]
  (-> theme theme-mapping prop))

(defn theme-identity
  [theme prop]
  (-> theme theme-mapping :reverse theme-mapping prop))

(defn get-theme-with
  [func]
  {:theme (func :theme)
   :theme-text (func :text)
   :theme-background (func :background)
   :theme-lines-color (func :lines-color)
   :theme-font-color (func :font-color)})
