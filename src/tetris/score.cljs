(ns tetris.score
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! put! chan]]
            [goog.dom :as dom]))

(def score-chan (chan))
(def score (dom/getElementByClass "score"))

(dom/setTextContent score "0")

(go (while true
      (let [completed (<! score-chan)]
        (when (pos? completed)
          (let [s (js/parseInt (dom/getTextContent score) 10)
                new-score (+ s completed)]
            (dom/setTextContent score new-score))))))
