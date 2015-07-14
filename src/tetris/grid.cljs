(ns tetris.grid
  (:require [monet.canvas :as canvas]
            [tetris.control :as control]
            [tetris.colours :as colours]
            [tetris.shapes :as shapes]))

;;ideas about completed lines
;;count all the complete lines in the grid
;;flatten the grid and shift each piece (* 10 count) to the right
;;unflatten
;;anything that is out of bounds is gone

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

(defn add-shape [pos shape colour]
  (swap! active-shape (fn [_]
                        {:shape shape
                         :colour colour
                         :pos pos})))

(defn add-random-shape []
  (add-shape [0 4] (shapes/random-shape) (colours/random)))

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

(defn transpose-cells [shape]
  (map #(add-vectors (:pos shape) %) (:shape shape)))

(defn add-or-remove-active-shape [grid shape update-fn] 
  (let [pos (:pos shape)
        colour (:colour shape)]
    (loop [cells (transpose-cells shape)
           g grid]
      (if (empty? cells) g
        (recur (rest cells) (update-in g (first cells) (update-fn shape)))))))

(defn remove-active-shape [grid shape] 
  (add-or-remove-active-shape grid shape (fn [_] (fn [_] 0))))

(defn add-active-shape [shape grid] 
  (add-or-remove-active-shape grid shape (fn [s] (fn [_] s))))

(defn grid-empty? [grid]
  (every? #(= 0 %) (flatten grid)))

(defn doesnt-overlap? 
  "does this shape overlap anything else in the grid other than itself"
  [grid cells]
  (not-any? #(not= 0 (get-in grid %)) cells))

(defn in-bounds? 
  "does this shape lie within the bounds of the grid"
  [cells]
  (not-any? (fn [[r c]] 
                (or (< r 0) (> r 13) (< c 0) (> c 9))) cells))

(defn valid-pos? [grid shape]
   (let [cells (transpose-cells shape)]
     (and (in-bounds? cells) 
          true
          (doesnt-overlap? grid cells))))

(defn shift-active-shape [grid dir]
  (let [p (vectorise dir)
        s @active-shape
        shifted (assoc s :pos (add-vectors p (:pos s)))]
    (if (valid-pos? grid shifted)
      (swap! active-shape (fn [_] shifted))
      s)))

(defn rotate [{:keys [shape] :as s}]
  (let [rotated (assoc s :shape (shapes/rotate shape :cw))]
    (if (in-bounds? rotated) rotated s)))

(defn rotate-active-shape [] 
 (swap! active-shape rotate))


(defn gravity [orig grid]
  "every half a second drop the active shape down"  
  (let [delta (get-delta)]
    (if (> delta 500)
      (do 
        (swap! tick now)
        (let [shifted (shift-active-shape grid :down)]
          (when (= orig shifted)
            ;;check for completed rows and remove them from the grid
            ;;moving everything above that row down
            (add-random-shape)
            orig))) 
      orig)))

(defn update-grid [grid]
  (let [shape @active-shape
        removed (remove-active-shape grid shape)
        kp [:left :right :down]]
    (doseq [k kp]
      (if (control/key-pressed? k)
        (shift-active-shape removed k))) 
    (when (control/key-pressed? :rotate)
      (rotate-active-shape))
    (control/reset-keys-pressed!)
    (-> @active-shape
        (gravity ,,, removed)
        (add-active-shape ,,, removed))))

(defn draw-background [] 
  (do 
    (draw-lines 11 :col)
    (draw-lines 15 :row)))

