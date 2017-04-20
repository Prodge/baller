(ns baller.text
  (:require [cljs.core.async :refer [timeout]]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.events :as e]
            [infinitelives.utils.coordinates :as coord]
            [baller.constants :as c])
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [baller.async :refer [go-while]]))

(defn slide [text speed]
  (go
    (loop [f 0]
      (let [[width _] (coord/get-window-size)
            [_ _ text-width _] (s/get-rects text)
            slide-width (+ width (* 2 text-width))
            restricted (mod (* speed f) slide-width)
            pos (- restricted (/ slide-width 2))]
        (s/set-x! text pos))
      (<! (e/next-frame))
      (recur (inc f)))))

(defn swipe-in [text & {:keys [speed]
                     :or {speed 2}}]
  (go
    (loop [f 0]
      (let [pos (- 0 (Math.pow (Math/pow speed 1.5) (- 11 (/ f 11))))]
        (s/set-x! text pos)
        (<! (e/next-frame))
        (when (< pos -1)
          (recur (inc f)))))))

(defn swipe-out [text & {:keys [speed]
                     :or {speed 2}}]
  (go
    (let [[width _] (coord/get-window-size)
          [_ _ text-width _] (s/get-rects text)
          slide-width (+ width (* 2 text-width))]
      (loop [f 0]
        (let [pos (Math.pow speed (/ f 20))]
          (s/set-x! text pos)
          (<! (e/next-frame))
          (when (< pos slide-width)
            (recur (inc f))))))))

(defn swipe [text & {:keys [speed pause loop? loop-while]
                     :or {speed 2 pause 0 loop? false loop-while #(true)}}]
  (go-while loop-while
    (loop [_ 0]
        (<! (swipe-in text speed))
        (<! (timeout (* pause 1000)))
        (<! (swipe-out text speed))
      (when loop? (recur _)))))

(defn push-in [text & {:keys [speed]
                       :or {speed 2}}]
  (go
    (let [[ _ original-scale] (s/get-scale text)]
      (loop [f 0]
        (let [scale (Math.pow (/ f 10) speed)]
          (s/set-scale! text scale)
          (<! (e/next-frame))
          (when (< scale original-scale)
            (recur (inc f))))))))

(defn push-over [text & {:keys [speed]
                       :or {speed 2}}]
  (go
    (let [[ _ original-scale] (s/get-scale text)]
      (loop [f 0]
        (let [scale (+ original-scale (Math.pow (/ f 10) (Math.pow speed (/ f 15))))
              alpha (/ (- (* speed 50) f) 100)]
          (s/set-scale! text scale)
          (s/set-alpha! text alpha)
          (<! (e/next-frame))
          (when (> alpha 0)
            (recur (inc f))))))))

(defn push-through [text & {:keys [speed pause]
                       :or {speed 2 pause 0}}]
  (go
    (<! (push-in text speed))
    (<! (timeout (* pause 1000)))
    (<! (push-over text speed))))

