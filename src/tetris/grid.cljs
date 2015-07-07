(ns tetris.grid
  (:require [monet.canvas :as canvas]
            [tetris.control :as control]
            [tetris.colours :as colours]
            [tetris.shapes :as shapes]))

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

;;what should the active shape look like?
;; { :shape
;;   :colour 
;;   :pos }

; (defn add-shape [pos shape]
;   (let [s (map (fn [c] 
;                  {:colour :g
;                   :pos (add-vectors pos c)}) shape)]
;     (swap! active-shape (fn [_] s))))

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

(defn add-or-remove-active-shape [grid update-fn] 
  (let [pos (:pos @active-shape)
        colour (:colour @active-shape)]
    (loop [cells (transpose-cells @active-shape)
           g grid]
      (if (empty? cells) g
        (recur (rest cells) (update-in g (first cells) (update-fn {:colour colour})))))))

(defn remove-active-shape [grid] 
  (add-or-remove-active-shape grid (fn [_] (fn [_] 0))))

(defn add-active-shape [grid] 
  (add-or-remove-active-shape grid (fn [s] (fn [_] s))))

(defn shift-active-shape [dir]
  (let [p (vectorise dir)]
    (swap! active-shape #(assoc % :pos (add-vectors p (:pos %))))))

(defn rotate [{:keys [shape] :as s}]
  (assoc s :shape (shapes/rotate shape :cw)))

(defn rotate-active-shape [] 
 (swap! active-shape rotate))

(defn gravity []
  "every half a second drop the active shape down"  
  (let [delta (get-delta)]
    (when (> delta 500)
      (do 
        (swap! tick now)
        (when @active-shape
          (shift-active-shape :down))))))

(defn update-grid [grid]
  (let [removed (remove-active-shape grid)
        kp [:left :right :down]]
    (doseq [k kp]
      (if (control/key-pressed? k)
        (shift-active-shape k))) 
    (when (control/key-pressed? :rotate)
      (rotate-active-shape))
    (control/reset-keys-pressed!)
    (gravity)
    (add-active-shape removed)))

(defn draw-background [] 
  (do 
    (draw-lines 11 :col)
    (draw-lines 15 :row)))

