(ns mapping.themes)

(def theme-mapping
  {:light {:reverse :dark
           :theme "dark"
           :text "Light Mode"
           :background "#121010"
           :font-color "white"}
   :dark {:reverse :light
          :theme "light"
          :text "Dark Mode"
          :background "white"
          :font-color "black"}})

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
   :theme-font-color (func :font-color)})
