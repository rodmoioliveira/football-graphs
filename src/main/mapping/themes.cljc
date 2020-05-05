(ns mapping.themes)

(def theme-mapping
  {:light {:reverse :dark
           :theme "dark"
           :text "dark"
           :background "#121212"
           :font-color "#f5f5f5"
           :lines-color "#333"
           :outline-node-color "#eee"
           :color-range #js ["#410804", "#f99d97"]}
   :dark {:reverse :light
          :theme "light"
          :text "light"
          :background "#f9f9f9"
          :font-color "#222"
          :lines-color "#ccc"
          :outline-node-color "#222"
          :color-range #js ["#ffd5d5", "#ff3c3c"]}})

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
   :theme-font-color (func :font-color)
   :theme-outline-node-color (func :outline-node-color)
   :theme-color-range (func :color-range)})
