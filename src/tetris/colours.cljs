(ns tetris.colours)

(def colours {:r "red"
              :b "blue"
              :y "yellow"
              :o "orange"
              :g "green"})

(defn random []
  (rand-nth [:r :b :y :o :g]))

(defn to-colour [k] 
  (k colours))


