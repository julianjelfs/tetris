(ns ^:figwheel-always tetris.core
  (:require [monet.canvas :as canvas]
            [tetris.shapes :as shapes]
            [goog.dom :as dom]
            [goog.events :as events]
            [goog.dom.classes :as classes]
            [tetris.colours :as colours]
            [tetris.grid :as grid]))

(enable-console-print!)

(def start-btn (dom/getElement "start"))

(events/listen start-btn "click"
               (fn [_]
                 (grid/init)
                 (classes/add start-btn "hidden")))

(defonce mc (canvas/init (.getElementById js/document "game-foreground") "2d"))

(defn now [] (.getTime (js/Date.)))

(grid/draw-background)

(defn update-grid 
  "delegate updating the grid to the grid ns"
  [grid]
  (grid/update-grid grid))

(defn draw-square [ctx colour x y]
  (let [size 46
        radius 3
        sx (+ size x)
        rx (+ radius x)
        sy (+ size y)
        ry (+ radius y)]
 (-> ctx
     (canvas/begin-path)
     (canvas/move-to ,,, rx y)
     (canvas/line-to ,,, (- sx radius) y)
     (canvas/quadratic-curve-to ,,, sx y sx ry)
     (canvas/line-to ,,, sx (- sy radius))
     (canvas/quadratic-curve-to ,,, sx sy (- sx radius) sy)
     (canvas/line-to ,,, rx sy)
     (canvas/quadratic-curve-to ,,, x sy x (- sy radius))
     (canvas/line-to ,,, x ry)
     (canvas/quadratic-curve-to ,,, x y rx y)
     (canvas/close-path)
     (canvas/stroke)
     (canvas/fill-style colour)
     (canvas/fill))))

(defn render-grid
  "grid is a 2d vec representing each cell as filled or not by val 0 or 1"
  [ctx grid]
  (doseq [r (range 14)]
    (doseq [c (range 10)]
      (let [cell (get-in grid [r c])
            x (* 50 c)
            y (* 50 r)]
        (when (not (= 0 cell))
          (draw-square ctx (colours/to-colour (:colour cell)) (+ 2 x) (+ 2 y)))))))

(canvas/add-entity mc
                   :grid
                   (canvas/entity grid/grid update-grid render-grid))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

