(ns tetris.score
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan]]
            [goog.dom :as dom]))

(def score-chan (chan))
(def score (dom/getElementByClass "score"))

(defn reset! [] 
  (dom/setTextContent score "0"))

(reset!)

(go (while true
      (let [completed (<! score-chan)]
        (when (pos? completed)
          (let [s (js/parseInt (dom/getTextContent score) 10)
                new-score (+ s completed)]
            (dom/setTextContent score new-score))))))
