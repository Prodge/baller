(ns baller.core
  (:require [infinitelives.pixi.canvas :as c]
              [infinitelives.pixi.events :as e]
              [infinitelives.pixi.resources :as r]
              [infinitelives.pixi.texture :as t]
              [infinitelives.pixi.sprite :as s]
              [goog.events :as events]
              [goog.events.EventType :as event-type]
              [cljs.core.async :refer [<!]])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.macros :as m]))

(enable-console-print!)

(defonce game-state (atom {:gravity 0.6
                           :bounces 0}))

(defonce hit-tolerance 30)
(defonce bounce-factor -15)
(defonce air-friction 0.98)
(defonce push-factor 2)
(defonce scale 3)

(defn on-js-reload []
  (println "Reloading Figwheel"))

(defn get-canvas-dimensions []
   [(.-innerHeight js/window)
    (.-innerWidth js/window)])

(defn off-screen? [sprite]
  (let [[height width] (get-canvas-dimensions)
        [x y] (s/get-xy sprite)]
    (or
      (> (-> y Math/abs (* 2)) height)
      (> (-> x Math/abs (* 2)) width))))

(defn mouse-impact? [ball]
  (let [{:keys [x y]} (:mouse @game-state)
        [ball-x ball-y] (s/get-xy ball)]
    (and (> (+ ball-x hit-tolerance) x)
         (< (- ball-x hit-tolerance) x)
         (> (+ ball-y hit-tolerance) y)
         (< (- ball-y hit-tolerance) y))))

(defn mouse-x-difference [ball]
  (let [{:keys [x]} (:mouse @game-state)
        ball-x (s/get-x ball)]
    (- ball-x x)))

(defn increment-bounces []
  (swap! game-state assoc :bounces (inc (:bounces @game-state))))

(defn reset-bounces []
  (swap! game-state assoc :bounces 0))

(defn game-thread [ball]
  (go
    (loop [pos-x 0
           pos-y 0
           vel-x 0
           vel-y 1]
      (let [bounce (mouse-impact? ball)]
        (when bounce (increment-bounces))
        (s/set-pos! ball pos-x pos-y)
        (<! (e/next-frame))
        (when (not (off-screen? ball))
          (recur (+ pos-x vel-x)
                 (+ pos-y vel-y)
                 (* (if bounce (/ (mouse-x-difference ball) push-factor) vel-x) air-friction)
                 (+ (if bounce bounce-factor vel-y) (:gravity @game-state))))))))

(defn canvas-coord-to-pixi [x y]
  (let [[height width] (get-canvas-dimensions)]
    [(- x (/ width 2))
     (- y (/ height 2))]))

(defn mouse-move-handler [event]
  (let [[x y] (canvas-coord-to-pixi (.-clientX event) (.-clientY event))]
    (swap! game-state assoc :mouse {:x x :y y})))

(defn titlescreen-thread []
  (go
    (println "Starting Game")))

(defn end-game-thread []
  (go
    (println "Game Over." (:bounces @game-state) "bounces!")))

(defonce canvas
  (c/init {:layers [:bg :ball :ui]
           :background 0x1099bb
           :expand true}))

(defonce init-handlers
  (events/listen js/window event-type/MOUSEMOVE #(mouse-move-handler %)))

(defonce main-thread
  (go
    (<! (r/load-resources canvas :ui ["img/ball.png"]))
    (t/set-texture! :ball (r/get-texture :ball :nearest))

    (m/with-sprite canvas :bg
      [ball (s/make-sprite :ball {:mousemove mouse-move-handler})]
        (while true
          (reset-bounces)
          (<! (titlescreen-thread))
          (<! (game-thread ball))
          (<! (end-game-thread))))))
