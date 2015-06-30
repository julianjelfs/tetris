(ns ^:figwheel-always tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [monet.canvas :as canvas]
            [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [tetris.shapes :as shapes]))

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

(def mc (canvas/init (.getElementById js/document "game") "2d"))

(def ctx (canvas/get-context (.getElementById js/document "game") "2d"))

(defn upd-fn [val]
  (assoc val :r (inc (:r val))))


(defn add-shape [pos shape]
 (doseq [e (shape->entities pos shape)]
  (canvas/add-entity mc (swap! entity-keys inc) e)))


; (defn draw-grid [lines]
;   (dotimes [n lines]
;     (let [coord (* n 50)]
;       (-> ctx 
;           (canvas/begin-path)
;           (canvas/stroke-style ,,, "#cccccc")
;           (canvas/move-to ,,, coord 0)
;           (canvas/line-to ,,, coord 700)
;           (canvas/stroke))
;       )))

;don't know why this one works but the one above doesn't
(defn line-from [ctx [sx sy] [ex ey]]
  (-> ctx 
      (canvas/move-to ,,, sx sy)
      (canvas/line-to ,,, ex ey)))

(defn draw-grid [lines dir]
  (dotimes [n lines]
    (let [coord (* n 50)
          start (if (= dir :col) [coord 0] [0 coord])
          end (if (= dir :col) [coord 700] [500 coord])]
    (canvas/add-entity mc (str "grid-" dir n)
                       (canvas/entity nil nil
                                      (fn [ctx _]
                                        (-> ctx 
                                            canvas/begin-path
                                            (canvas/stroke-style ,,, "#cccccc")
                                            (line-from ,,, start end)
                                            canvas/stroke)))))))

(draw-grid 11 :col)
(draw-grid 15 :row)
; (add-shape [100 100] shapes/spiece)


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

