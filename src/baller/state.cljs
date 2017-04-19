(ns baller.state
  (:require [baller.constants :refer [bounce-protection]]))

; State initialization

(defonce default-state {:gravity 0.6
                        :bounces 0
                        :bounce-protection 0
                        :playing true
                        :mouse {:x 9999 :y 9999}})

(defonce game-state (atom default-state))


; State Updates

(defn reset-state! []
  (reset! game-state default-state))

(defn set-bounce-protection! []
  (swap! game-state assoc :bounce-protection bounce-protection))

(defn dec-bounce-protection! []
  (let [bp (:bounce-protection @game-state)]
    (when (pos? bp)
      (swap! game-state assoc :bounce-protection (dec bp)))))

(defn increment-bounces! []
  (swap! game-state assoc :bounces (inc (:bounces @game-state))))

(defn set-mouse-position! [[x y]]
  (swap! game-state assoc :mouse {:x x :y y}))


; State queries

(defn mouse-pos? []
  (:mouse @game-state))

(defn bounce-protection? []
  (:bounce-protection @game-state))

(defn gravity? []
  (:gravity @game-state))

(defn bounces? []
  (:bounces @game-state))
