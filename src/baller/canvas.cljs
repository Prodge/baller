(ns baller.canvas
   (:require [infinitelives.pixi.canvas :as canvas]
             [baller.constants :refer [canvas-colour]]))

(defonce canvas
  (canvas/init {:layers [:bg :ball :score]
           :background canvas-colour
           :expand true
           :origins {:score :top-right}}))
