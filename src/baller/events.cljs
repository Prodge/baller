(ns baller.events
  (:require [goog.events :as events]
            [goog.events.EventType :as event-type]
            [infinitelives.utils.coordinates :refer [origin-top-left->center]]
            [baller.state :refer [set-mouse-position!]]))

(defn mouse-move-handler [event]
  (set-mouse-position!
    (origin-top-left->center (.-clientX event) (.-clientY event))))

(defn mouse-listener []
  (events/listen js/window event-type/MOUSEMOVE mouse-move-handler))
