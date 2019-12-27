(ns reactive.app
	(:require
		; [reactive.rxjs :refer [move-mouse$]]
		[reactive.graph :refer [init-graph]]))


(defn init []
	(init-graph)
	; (move-mouse$)
	)

; TODO: https://code.thheller.com/blog/shadow-cljs/2019/08/25/hot-reload-in-clojurescript.html
(defn ^:dev/after-load start []
	(init))
