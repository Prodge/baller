(ns baller.core
  (:require [cljs.core.async :refer [timeout]]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.coordinates :as coord]
            [infinitelives.utils.events :refer [is-pressed?]]
            [goog.events :as events]
            [goog.events.EventType :as event-type]
            [cljs.core.async :refer [<!]]
            [baller.constants :as c]
            [baller.init :as init]
            [baller.state :as state]
            [baller.events :as baller-events]
            [baller.utils :as utils]
            [baller.text :as text]
            [baller.canvas :refer [canvas]])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.macros :as m]
                     [baller.async :refer [go-while]]
                     [infinitelives.pixi.pixelfont :as pf]))

(enable-console-print!)

(defn on-js-reload []
  (println "Reloading Figwheel"))

(defn game-thread [ball]
  (go
    (s/set-visible! ball true)
    (loop [pos-x 0
           pos-y -200
           vel-x 0
           vel-y 1]
      (let [bounce (and (utils/mouse-impact? ball) (zero? (state/bounce-protection?)))]
        (if bounce
          (do (state/increment-bounces!)
              (state/set-bounce-protection!))
          (state/dec-bounce-protection!))
        (s/set-pos! ball pos-x pos-y)
        (<! (e/next-frame))
        (if (not (utils/off-screen? ball))
          (recur (+ pos-x vel-x)
                 (+ pos-y vel-y)
                 (* (if bounce (/ (utils/mouse-x-difference ball) c/push-factor) vel-x) c/air-friction)
                 (+ (if bounce c/bounce-velocity vel-y) (state/gravity?)))
          (s/set-visible! ball false))))))

(defn score-thread []
  (go
    (m/with-sprite canvas :score
      [score-text (pf/make-text :small "0"
                                :scale 4
                                :x -80 :y 40)]
      (loop [score (state/bounces?)]
        (s/set-visible! score-text (state/playing?))
        (let [new-score (state/bounces?)]
          (when (not= new-score score)
            (pf/change-text! score-text :small (str (int new-score)))
            (text/bump score-text :speed 5))
        (<! (e/next-frame))
        (recur new-score))))))

(defn titlescreen-thread []
  (go-while (and (not (is-pressed? :space)) (not (state/touching?)))
    (m/with-sprite canvas :ui
      [press-space (pf/make-text :small "Press Space or Touch to Start"
                                :scale 2
                                :visible false
                                :y 150)
       get-ready (pf/make-text :small "Smack The Ball"
                                :scale 3
                                :visible false
                                :y 50)
       baller (pf/make-text :small "Baller"
                                :scale 5)]
      (text/swipe baller :speed 1.5 :pause 2 :loop? true :loop-while #(not (is-pressed? :space)))
      (<! (timeout c/title-spacing))
      (s/set-visible! get-ready true)
      (text/swipe get-ready :pause 1 :loop? true :loop-while #(not (is-pressed? :space)))
      (<! (timeout c/title-spacing))
      (s/set-visible! press-space true)
      (text/swipe press-space :speed 3 :loop? true :loop-while #(not (is-pressed? :space)))
      (while true
        (<! (e/next-frame))))))

(defn end-game-thread []
  (go
    (m/with-sprite canvas :ui
      [game-over (pf/make-text :small "Game Over"
                                :scale 6
                                :y 0)
       score (pf/make-text :small (str "Score: " (state/bounces?))
                                :scale 3
                                :visible false
                                :y 100)]
      (text/push-through game-over :pause 1)
      (<! (timeout c/title-spacing))
      (s/set-visible! score true)
      (<! (text/push-through score :speed 3 :pause 2)))))

(defn advance-difficulty []
  (go-while (state/playing?)
    (loop [gravity (state/gravity?)]
      (state/set-gravity! gravity)
      (<! (e/next-frame))
      (recur (+ gravity c/gravity-increase)))))

(defonce main-thread
  (go
    (<! (init/resources))
    (init/font)
    (init/textures)
    (init/handlers)

    (m/with-sprite canvas :bg
      [ball (s/make-sprite :ball :visible false)]
        (score-thread)
        (while true
          (state/reset-state!)
          (<! (titlescreen-thread))
          (state/set-playing! true)
          (advance-difficulty)
          (<! (game-thread ball))
          (state/set-playing! false)
          (<! (end-game-thread))))))
