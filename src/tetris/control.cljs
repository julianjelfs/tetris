(ns tetris.control
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]))

(def w (dom/getWindow))
(defonce keys-pressed (atom {}))
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

(defn reset-keys-pressed! []
  (reset! keys-pressed {}))

(defn key-pressed! [k] 
  (swap! keys-pressed assoc k true))

(defn key-pressed? [k]
  (k @keys-pressed))

(let [chan (control-events)]
  (go (while true
        (let [k (<! chan)]
          (key-pressed! k)))))
 

