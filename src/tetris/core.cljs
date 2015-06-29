(ns ^:figwheel-always tetris.core
  (:require [monet.canvas :as canvas]
            [tetris.shapes :as shapes]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defonce entity-keys (atom 0))

(defn update-entity [val]
  (let [now (.getTime (js/Date.))
        updated (:updated val)
        delta (- now updated)] 
    (if (> delta 500)
      (assoc val :updated now :y (+ 50 (:y val))) 
      val)))

(defn shape->entities [[px py] shape]
 (map (fn [[x y]]
        (canvas/entity {:updated (.getTime (js/Date.)) 
                        :x (+ px (* x 50)) 
                        :y (+ py (* y 50)) 
                        :w 50 
                        :h 50 
                        :shape shape}
                       update-entity
                       (fn [ctx val]             ; draw function
                         (-> ctx
                             (canvas/fill-style "red")
                             (canvas/fill-rect val))))) shape))

(def canvas-dom (.getElementById js/document "game"))

(def mc (canvas/init canvas-dom "2d"))

(defn upd-fn [val]
  (assoc val :r (inc (:r val))))


(defn add-shape [pos shape]
 (doseq [e (shape->entities pos shape)]
  (canvas/add-entity mc (swap! entity-keys inc) e)))


; (canvas/add-entity mc :background
;                    (canvas/entity {:x 250 :y 250 :r 100} ; val
;                                   nil                       ; update function
;                                   (fn [ctx val]             ; draw function
;                                     (-> ctx
;                                         (canvas/fill-style "red")
;                                         (canvas/circle val)
;                                         (canvas/fill)
;                                         ))))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

