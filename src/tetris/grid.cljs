(ns tetris.grid
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [monet.canvas :as canvas]
            [tetris.control :as control]
            [tetris.shapes :as shapes]
            [cljs.core.async :refer [<!]]))

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

(defn add-vectors [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defonce active-shape (atom nil))

(defn add-shape [pos shape]
  (let [s (map (fn [c] 
                 {:colour :g
                  :pos (add-vectors pos c)}) shape)]
    (swap! active-shape (fn [_] s))))

(defn add-random-shape []
  (add-shape [0 4] (shapes/random-shape)))

(defn init []
  (add-random-shape))

(defn vectorise [dir]
  (condp = dir
    :left [0 -1]
    :right [0 1]
    :down [1 0]))

(defn now [] (.getTime (js/Date.)))
(def tick (atom (now)))

(defn get-delta [] 
 (- (now) @tick))

(defn add-or-remove-active-shape [grid update-fn] 
  (loop [s @active-shape
         g grid]
    (if (empty? s) g
        (recur (rest s) (update-in g (:pos (first s)) (update-fn (first s)))))))

(defn remove-active-shape [grid] 
  (add-or-remove-active-shape grid (fn [_] (fn [_] 0))))

(defn add-active-shape [grid] 
  (add-or-remove-active-shape grid (fn [s] (fn [_] s))))

(defn shift-active-shape [dir]
  (let [p (vectorise dir)]
    (swap! active-shape (fn [shape]
                          (map (fn [s]
                                 (assoc s :pos (add-vectors (:pos s) p))) shape)))))

(defn update-grid [grid]
  (let [delta (get-delta)
        removed (remove-active-shape grid)]    
    (when (> delta 500)
      (do 
        (swap! tick now)
        (when @active-shape
          (shift-active-shape :down))))
    (add-active-shape removed)))

(defn draw-background [] 
  (do 
    (draw-lines 11 :col)
    (draw-lines 15 :row)))

(defonce control-events (control/control-events))

(go (while true
      (let [k (<! control-events)]
        (condp = k
          :left (prn "left")
          :right (prn "right")
          :down (prn "down")
          :rotate (prn "rotate")))))
