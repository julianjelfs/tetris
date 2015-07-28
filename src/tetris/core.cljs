(ns ^:figwheel-always tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [monet.canvas :as canvas]
            [tetris.shapes :as shapes]
            [goog.dom :as dom]
            [goog.events :as events]
            [goog.dom.classes :as classes]
            [goog.style :as style]
            [tetris.colours :as colours]
            [tetris.grid :as grid]
            [tetris.score :as score]
            [cljs.core.async :refer [<! put! chan]]))

(enable-console-print!)

(def start-btn (dom/getElement "start"))
(def restart-btn (dom/getElement "restart"))
(def foreground (dom/getElement "game-foreground"))

(events/listen start-btn "click"
               (fn [_]
                 (grid/init)
                 (classes/add start-btn "hidden")))

(defonce mc (canvas/init foreground "2d"))

(grid/draw-background)

(def game-over-chan (chan))
(defn update-grid 
  "delegate updating the grid to the grid ns"
  [grid]
  (grid/update-grid grid game-over-chan))

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

(defn start []
 (canvas/add-entity mc
                   :grid
                   (canvas/entity grid/grid update-grid render-grid)))

(start)

(events/listen restart-btn "click"
               (fn [_]
                 (score/reset!)
                 (canvas/remove-entity mc :grid)
                 (start)
                 (classes/add restart-btn "hidden")))

(go (while true
      (when (<! game-over-chan)
        (classes/remove restart-btn "hidden"))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

