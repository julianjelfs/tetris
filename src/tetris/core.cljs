(ns ^:figwheel-always tetris.core
  (:require [monet.canvas :as canvas]
            [tetris.shapes :as shapes]
            [tetris.grid :as grid]))

(enable-console-print!)

(def mc (canvas/init (.getElementById js/document "game-foreground") "2d"))

(def colours {:r "red"
              :b "blue"
              :y "yellow"
              :o "orange"
              :g "green"})

(defn now [] (.getTime (js/Date.)))

(grid/draw-background)

(defn update-grid 
  "delegate updating the grid to the grid ns"
  [grid]
  (grid/update-grid grid))

(defn render-grid
  "grid is a 2d vec representing each cell as filled or not by val 0 or 1"
  [ctx grid]
  (doseq [r (range 14)]
    (doseq [c (range 10)]
      (let [cell (get-in grid [r c])
            x (* 50 c)
            y (* 50 r)]
        (when (not (= 0 cell))
          (-> ctx
              (canvas/begin-path)
              (canvas/fill-style ,,, ((:colour cell) colours))
              (canvas/fill-rect ,,, {:x x :y y :w 50 :h 50})))))))

(canvas/add-entity mc
                   :grid
                   (canvas/entity grid/grid update-grid render-grid))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

