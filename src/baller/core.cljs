(ns baller.core
  (:require [infinitelives.pixi.canvas :as canvas]
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
            [baller.canvas :refer [canvas]])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.macros :as m]
                     [infinitelives.pixi.pixelfont :as pf]))

(enable-console-print!)

(defn on-js-reload []
  (println "Reloading Figwheel"))

(defn off-screen? [sprite]
  (let [[height width] (coord/get-window-size)
        [x y] (s/get-xy sprite)]
    (or
      (> (-> y Math/abs (* 2)) height)
      (> (-> x Math/abs (* 2)) width))))

(defn mouse-impact? [ball]
  (let [{:keys [x y]} (state/mouse-pos?)
        [ball-x ball-y] (s/get-xy ball)]
    (and (> (+ ball-x c/hit-tolerance) x)
         (< (- ball-x c/hit-tolerance) x)
         (> (+ ball-y c/hit-tolerance) y)
         (< (- ball-y c/hit-tolerance) y))))

(defn mouse-x-difference [ball]
  (let [{:keys [x]} (state/mouse-pos?)
        ball-x (s/get-x ball)]
    (- ball-x x)))

(defn game-thread [ball]
  (go
    (s/set-visible! ball true)
    (loop [pos-x 0
           pos-y 0
           vel-x 0
           vel-y 1]
      (let [bounce (and (mouse-impact? ball) (zero? (state/bounce-protection?)))]
        (if bounce
          (do (state/increment-bounces!)
              (state/set-bounce-protection!))
          (state/dec-bounce-protection!))
        (s/set-pos! ball pos-x pos-y)
        (<! (e/next-frame))
        (if (not (off-screen? ball))
          (recur (+ pos-x vel-x)
                 (+ pos-y vel-y)
                 (* (if bounce (/ (mouse-x-difference ball) c/push-factor) vel-x) c/air-friction)
                 (+ (if bounce c/bounce-velocity vel-y) (state/gravity?)))
          (s/set-visible! ball false))))))

(defn score-thread []
  (go
    (m/with-sprite canvas :score
      [score-text (pf/make-text :small "0"
                                :scale 4
                                :x -80 :y 20)]
      (loop [score (state/bounces?)]
        (s/set-visible! score-text (state/playing?))
        (let [new-score (state/bounces?)]
          (when (not= new-score score)
            (pf/change-text! score-text :small (str (int new-score))))
        (<! (e/next-frame))
        (recur new-score))))))

(defn titlescreen-thread []
  (go
    (m/with-sprite canvas :ui
      [score-text (pf/make-text :small "Press Space to start"
                                :scale 2)]
      (loop [frame-num 0]
        (<! (e/next-frame))
        (when (not (is-pressed? :space))
          (recur (inc frame-num)))))))

(defn end-game-thread []
  (go
    (println "Game Over." (state/bounces?) "bounces!")))

(defonce main-thread
  (go
    (<! (init/resources))
    (init/font)
    (init/textures)
    (init/handlers)

    (m/with-sprite canvas :bg
      [ball (s/make-sprite :ball :alpha 0)]
        (score-thread)
        (while true
          (state/reset-state!)
          (<! (titlescreen-thread))
          (state/set-playing! true)
          (<! (game-thread ball))
          (state/set-playing! false)
          (<! (end-game-thread))))))
