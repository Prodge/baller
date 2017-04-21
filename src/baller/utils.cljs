(ns baller.utils)

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
