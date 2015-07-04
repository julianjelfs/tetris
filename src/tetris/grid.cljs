(ns tetris.grid
  (:require [monet.canvas :as canvas]))

(def grid (vec (for [r (range 14)]
                 (vec (map (fn [n] 0) (range 10))))))

(def ctx (canvas/get-context (.getElementById js/document "game-background") "2d"))

(defn line-from [ctx [sx sy] [ex ey]]
  (-> ctx 
      (canvas/move-to ,,, sx sy)
      (canvas/line-to ,,, ex ey)))

(defn draw-lines [lines dir]
  (dotimes [n lines]
    (let [coord (* n 50)
          start (if (= dir :col) [coord 0] [0 coord])
          end (if (= dir :col) [coord 700] [500 coord])]
      (-> ctx 
          (canvas/begin-path)
          (canvas/stroke-style ,,, "#cccccc")
          (line-from ,,, start end)
          (canvas/stroke)))))

(defn update-cell [grid pos f]
  (update-in grid pos f))

(defn add-shape [grid [r c] shape]
  (doseq [s shape]
    () ))

(defn now [] (.getTime (js/Date.)))
(def tick (atom (now)))
(def active-shape (atom nil))

(defn get-delta [] 
 (- (now) @tick))

(defn update-grid [grid]
  (let [delta (get-delta)]
    (when (> delta 1000)
      ;;move the active shape down a slot
      (prn "this should fire once per tick")
      (swap! tick now))
    ; (update-cell grid [7 7] (fn [_] 1))
    grid))


(defn draw-background [] 
  (do 
    (draw-lines 11 :col)
    (draw-lines 15 :row)))
