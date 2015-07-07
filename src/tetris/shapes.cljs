(ns tetris.shapes)

;;this contains the main logic for rotating tetris shapes and 
;;controlling their relative positions.
;;Borrowed heavily from https://github.com/jcollard/elmtris

(def line [[0 0] [1 0] [2 0] [3 0]])
(def square [[0 0] [1 0] [0 1] [1 1]])
(def zpiece [[0 0] [1 0] [1 1] [2 1]])
(def spiece [[1 0] [2 0] [0 1] [1 1]])
(def jpiece [[1 0] [1 1] [0 2] [1 2]])
(def lpiece [[0 0] [0 1] [0 2] [1 2]])
(def tpiece [[0 0] [1 0] [2 0] [1 1]])

(def shapes [line square zpiece spiece jpiece lpiece tpiece])

(defn random-shape []
  (rand-nth shapes))

(defn shift 
  "adjust a shape by the specified offset values"
  [[offx offy] shape]
  (map (fn [[x y]]
         [(+ x offx) (+ y offy)]) shape))

(defn bounds 
  "get the min x and min y and the max x and max y for this shape"
  [shape]
  (let [xs (map first shape)
        ys (map second shape)]
    [[ (apply min xs) (apply min ys)] [(apply max xs) (apply max ys)]]))

(defn dimensions 
  "get the x and y dimensions of the shape" 
  [shape]
  (let [[[minx miny] [maxx maxy]] (bounds shape)]
    [(- maxy miny) (- maxx minx)]))

(defn round [x]
  (let [fx (Math/floor x)]
    (if (> (- x fx) 0.5)
      (inc fx)
      fx)))

(defn centerOfMass 
  "find the center of mass of the shape" 
  [shape]
  (let [[r c] (dimensions shape)
        [sumx sumy tot] (reduce (fn [[x y t] [x1 y1]] 
                                  [(+ x x1) (+ y y1) (inc t)]) [0 0 0] shape)]
    [(round (/ sumy tot)) (round (/ sumx tot))]))

(defmulti rotate (fn [_ rot] rot))
(defmethod rotate :ccw 
  [shape _]
  (-> (rotate shape :cw)
      (rotate ,,, :cw)
      (rotate ,,, :cw)))

(defmethod rotate :cw 
  [shape _]
  (let [[[minx miny] [maxx maxy]] (bounds shape)
        [rows cols] (dimensions shape)
        [rC cC] (centerOfMass shape)
        trans (shift [(- cC) (- rC)] shape)
        off (if (or (= rows cols) (= rows 3)) -1 0)
        rt (fn [[x y]] [(- y) (+ x off)])
        rotated (map rt trans)]
    (shift [cC rC] rotated)))

; (defmethod rotate :cw 
;   [shape _]
;   (map (fn [[x y]] [(- y) x]) shape))
