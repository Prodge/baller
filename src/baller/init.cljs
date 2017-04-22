(ns baller.init
  (:require [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.pixelfont :as pf]
            [cljs.core.async :refer [<!]]
            [goog.events :as events]
            [goog.events.EventType :as event-type]
            [baller.events :as e]
            [baller.canvas :refer [canvas]])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.pixelfont :as pf]))


(defn font []
  (pf/pixel-font :small "img/fonts.png" [11 117] [235 169]
                 :chars ["ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                         "abcdefghijklmnopqrstuvwxyz"
                         "0123456789!?#`'.,-"]
                 :kerning {"fo" -2  "ro" -1 "la" -1 }
                 :space 5))

(defn resources []
  (go (<! (r/load-resources canvas :ball ["img/ball.png"
                                          "img/fonts.png"]))))

(defn textures []
  (t/set-texture! :ball (r/get-texture :ball :nearest)))

(defn handlers []
  (e/mouse-listener)
  (e/touch-listener)
  (e/touch-start-listener)
  (e/touch-end-listener))
