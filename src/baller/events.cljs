(ns baller.events
  (:require [goog.events :as events]
            [goog.events.EventType :as event-type]
            [infinitelives.utils.coordinates :refer [origin-top-left->center]]
            [baller.state :refer [set-mouse-position! set-touch-status!]]))

; Handlers

(defn mouse-move-handler [event]
  (set-mouse-position!
    (origin-top-left->center (.-clientX event) (.-clientY event))))

(defn touch-status-handler [status event]
  (set-touch-status! status))

; Listeners

(defn mouse-listener []
  (events/listen js/window event-type/MOUSEMOVE mouse-move-handler))

(defn touch-listener []
  (events/listen js/window event-type/TOUCHMOVE mouse-move-handler))

(defn touch-start-listener []
  (events/listen js/window event-type/TOUCHSTART (partial touch-status-handler true)))

(defn touch-end-listener []
  (events/listen js/window event-type/TOUCHEND (partial touch-status-handler false)))
