(ns mapping.themes)

(def theme-mapping
  {:light {:reverse :dark
           :theme "dark"
           :text "dark"
           :background "#121212"
           :font-color "#f5f5f5"
           :lines-color "#333"
           :outline-node-color "#eee"
           ; https://www.colorbox.io/
           :node-color-range #js ["#2C2A2A", "#FF4D00"]
           :edge-color-range #js ["#2C2A2A", "#FF4D00"]}
   :dark {:reverse :light
          :theme "light"
          :text "light"
          :background "#f9f9f9"
          :font-color "#222"
          :lines-color "#ccc"
          :outline-node-color "#222"
          ; https://www.colorbox.io/
          :node-color-range #js ["#FFF2F2", "#FF4D00"]
          :edge-color-range #js ["#FFD9D9", "#FF4D00"]}})

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
   :theme-node-color-range (func :node-color-range)
   :theme-edge-color-range (func :edge-color-range)})
