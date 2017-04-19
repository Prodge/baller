(ns baller.core
  (:require [infinitelives.pixi.canvas :as canvas]
              [infinitelives.pixi.events :as e]
              [infinitelives.pixi.resources :as r]
              [infinitelives.pixi.texture :as t]
              [infinitelives.pixi.sprite :as s]
              [infinitelives.pixi.pixelfont :as pf]
              [goog.events :as events]
              [goog.events.EventType :as event-type]
              [cljs.core.async :refer [<!]]
              [baller.constants :as c])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [infinitelives.pixi.macros :as m]
                     [infinitelives.pixi.pixelfont :as pf]))

(enable-console-print!)

(defonce default-state {:gravity 0.6
                        :bounces 0
                        :bounce-protection 0
                        :playing true
                        :mouse {:x 9999 :y 9999}})

(defonce game-state (atom default-state))

(defonce canvas
  (canvas/init {:layers [:bg :ball :score]
           :background c/canvas-colour
           :expand true
           :origins {:score :top-right}
           }))

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

(defn playing? []
  (:playing @game-state))

(defn score-thread []
  (go
    (while (playing?)
      (m/with-sprite canvas :score
        [score-text (pf/make-text :small "0"
                                  :scale 4
                                  :x -80 :y 20)]
        (loop [score (:bounces @game-state)]
          (let [new-score (:bounces @game-state)]
            (when (not= new-score score)
              (.removeChildren score-text)
              (js/console.log new-score)
              ;(pf/change-text! score-text :small (str (int new-score)))
              )
          (<! (e/next-frame))
          (recur new-score)))))))

(defn titlescreen-thread []
  (go
    (println "Starting Game")))

(defn end-game-thread []
  (go
    (println "Game Over." (:bounces @game-state) "bounces!")))

(defn init-font []
  (pf/pixel-font :small "img/fonts.png" [11 117] [235 169]
                 :chars ["ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                         "abcdefghijklmnopqrstuvwxyz"
                         "0123456789!?#`'.,-"]
                 :kerning {"fo" -2  "ro" -1 "la" -1 }
                 :space 5))

(defn init-resources []
  (go (<! (r/load-resources canvas :ball ["img/ball.png"
                                          "img/fonts.png"]))))

(defn init-textures []
  (t/set-texture! :ball (r/get-texture :ball :nearest)))

(defonce init-handlers
  (events/listen js/window event-type/MOUSEMOVE #(mouse-move-handler %)))

(defonce main-thread
  (go
    (<! (init-resources))
    (init-font)
    (init-textures)

    (m/with-sprite canvas :bg
      [ball (s/make-sprite :ball {:mousemove mouse-move-handler})]
        (while true
          (reset-state)
          (score-thread)
          (<! (titlescreen-thread))
          (<! (game-thread ball))
          (<! (end-game-thread))))))
