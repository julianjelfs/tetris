(ns ^:figwheel-always tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [monet.canvas :as canvas]
            [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [tetris.shapes :as shapes]
            [tetris.grid :as grid]))

(enable-console-print!)

(def w (dom/getWindow))

(def mc (canvas/init (.getElementById js/document "game-foreground") "2d"))

(defonce entity-keys (atom 0))

(def controlKeyCodes #{37 38 39 40})
(def keyActions {37 :left
                 39 :right
                 40 :down
                 38 :rotate})

(defn controlCode [e]
  (contains? controlKeyCodes (.-keyCode e)))

(defn code->action [e]
  (get keyActions (.-keyCode e)))

(defn listen [el type]
  (let [out (chan 1 (comp 
                      (filter controlCode) 
                      (map code->action)))]
    (events/listen el type
                   #(put! out %))
    out))

(defn active-entities []
  (let [entities (:entities mc)
        ks (js-keys entities)
        vals (map (fn [k] 
                   (let [v (:value (aget entities k))]
                     (assoc v :k k))) ks)]
    (filter :active vals)))

(defn update-active-entites [f]
  (doseq [e (active-entities)]
    (prn e)
    (canvas/update-entity mc (:k e) f)))

(let [presses (listen w "keydown")]
  (go (while true
        (let [k (<! presses)]
          (condp = k
            :left (update-active-entites #(assoc % :x (- 50 (:x %))))
            :right (update-active-entites #(assoc % :x (+ 50 (:x %))))
            :down (prn "down")
            :rotate (prn "rotate"))))))

(defn now [] (.getTime (js/Date.)))

(defn update-entity [val]
  (let [now (.getTime (js/Date.))
        updated (:updated val)
        delta (- now updated)] 
      (if (> delta 500)
        ; (assoc val :updated now :y (+ 50 (:y val))) 
        val
        val)))

(defn shape->entities [[px py] shape]
 (map (fn [[x y]]
        (canvas/entity {:updated (.getTime (js/Date.)) 
                        :x (+ px (* x 50)) 
                        :y (+ py (* y 50)) 
                        :w 50 
                        :h 50 
                        :active true
                        :shape shape}
                       update-entity
                       (fn [ctx val]             ; draw function
                         (-> ctx
                             (canvas/fill-style "red")
                             (canvas/fill-rect val))))) shape))

(defn add-shape [pos shape]
 (doseq [e (shape->entities pos shape)]
  (canvas/add-entity mc (swap! entity-keys inc) e)))

(grid/draw)
(add-shape [100 100] shapes/spiece)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

