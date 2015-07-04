(ns tetris.control
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan]]))

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

(defn control-events []
  (let [out (chan 1 (comp 
                      (filter controlCode) 
                      (map code->action)))]
    (events/listen w "keydown"
                   #(put! out %))
    out))

