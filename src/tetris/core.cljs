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

(let [presses (listen w "keydown")]
  (go (while true
        (prn (<! presses)))))

(defonce entity-keys (atom 0))

(defn now [] (.getTime (js/Date.)))

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

(def mc (canvas/init (.getElementById js/document "game-foreground") "2d"))

(defn upd-fn [val]
  (assoc val :r (inc (:r val))))

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

