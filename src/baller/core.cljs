(ns baller.core
  (:require [infinitelives.pixi.canvas :as canvas]
              [infinitelives.pixi.events :as e]
              [infinitelives.pixi.resources :as r]
              [infinitelives.pixi.texture :as t]
              [infinitelives.pixi.sprite :as s]
              [goog.events :as events]
              [goog.events.EventType :as event-type]
              [cljs.core.async :refer [<!]]
              [baller.constants :as c])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.macros :as m]))

(enable-console-print!)

(defonce default-state {:gravity 0.6
                        :bounces 0
                        :bounce-protection 0
                        :mouse {:x 9999 :y 9999}})

(defonce game-state (atom default-state))

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
    (and (> (+ ball-x c/hit-tolerance) x)
         (< (- ball-x c/hit-tolerance) x)
         (> (+ ball-y c/hit-tolerance) y)
         (< (- ball-y c/hit-tolerance) y))))

(defn mouse-x-difference [ball]
  (let [{:keys [x]} (:mouse @game-state)
        ball-x (s/get-x ball)]
    (- ball-x x)))

(defn increment-bounces []
  (swap! game-state assoc :bounces (inc (:bounces @game-state))))

(defn reset-state []
  (reset! game-state default-state))

(defn set-bounce-protection []
  (swap! game-state assoc :bounce-protection c/bounce-protection))

(defn dec-bounce-protection []
  (let [bp (:bounce-protection @game-state)]
    (when (pos? bp)
      (swap! game-state assoc :bounce-protection (dec bp)))))

(defn game-thread [ball]
  (go
    (loop [pos-x 0
           pos-y 0
           vel-x 0
           vel-y 1]
      (let [bounce (and (mouse-impact? ball) (zero? (:bounce-protection @game-state)))]
        (if bounce
          (do (increment-bounces)
              (set-bounce-protection))
          (dec-bounce-protection))
        (s/set-pos! ball pos-x pos-y)
        (<! (e/next-frame))
        (when (not (off-screen? ball))
          (recur (+ pos-x vel-x)
                 (+ pos-y vel-y)
                 (* (if bounce (/ (mouse-x-difference ball) c/push-factor) vel-x) c/air-friction)
                 (+ (if bounce c/bounce-velocity vel-y) (:gravity @game-state))))))))

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
  (canvas/init {:layers [:bg :ball :ui]
           :background c/canvas-colour
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
          (reset-state)
          (<! (titlescreen-thread))
          (<! (game-thread ball))
          (<! (end-game-thread))))))
