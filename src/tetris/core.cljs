(ns ^:figwheel-always tetris.core
    (:require [jayq.core :refer [$ css html]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn drawSquare [colour pos])
(defn drawLong [colour pos])

(def shapes {:square drawSquare
             :long drawLong})

(defn drawShape! [type args]
  ((type shapes) args))

(defn init []
  (let [c ($ :#game) ]
    (prn c)))

(defn changeHeader [txt]
  (-> ($ "#header")
      (css {:color "red"})
      (html txt)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

