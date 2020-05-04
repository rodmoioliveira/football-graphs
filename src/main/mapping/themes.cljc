(ns mapping.themes)

(def theme-mapping
  {:light {:reverse :dark
           :theme "dark"
           :text "dark"
           :background "#121212"
           :font-color "#f5f5f5"
           :lines-color "#333"}
   :dark {:reverse :light
          :theme "light"
          :text "light"
          :background "#f9f9f9"
          :font-color "#222"
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
