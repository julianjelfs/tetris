(ns tetris.colours)

(def colours {:r "red"
              :b "blue"
              :y "yellow"
              :p "purple"
              :pk "pink"
              :o "orange"
              :g "green"})

(defn random []
  (rand-nth [:r :b :y :o :g]))

(defn to-colour [k] 
  (k colours))


