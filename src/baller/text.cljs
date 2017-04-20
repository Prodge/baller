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

(defn swipe [text & {:keys [speed pause loop? loop-while]
                     :or {speed 2 pause 0 loop? false loop-while #(true)}}]
  (go-while loop-while
    (while loop?
      (let [[width _] (coord/get-window-size)
            [_ _ text-width _] (s/get-rects text)
            slide-width (+ width (* 2 text-width))]
        (loop [f 0]
          (let [pos (- 0 (Math.pow speed (- 10 (/ f 10))))]
            (s/set-x! text pos)
            (<! (e/next-frame))
            (when (< pos -1)
              (recur (inc f)))))
        (<! (timeout (* pause 1000)))
        (loop [f 0]
          (let [pos (Math.pow speed (/ f 20))]
            (s/set-x! text pos)
            (<! (e/next-frame))
            (when (< pos slide-width)
              (recur (inc f)))))))))
