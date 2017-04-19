(ns baller.utils)

(defn get-canvas-dimensions []
   [(.-innerHeight js/window)
    (.-innerWidth js/window)])

(defn origin-top-left->center [x y]
  (let [[height width] (get-canvas-dimensions)]
    [(- x (/ width 2))
     (- y (/ height 2))]))

