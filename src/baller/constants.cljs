(ns baller.constants)

; Hitbox width around mouse
(def hit-tolerance 30)

; Y axis velocity for bounce
(def bounce-velocity -19)

; Amount of resistance along the x axis. 1 is no resistance
(def air-friction 0.98)

; Amount to reduce sideways bounces
(def push-factor 2)

; Min number of frames between bounces
(def bounce-protection 5)

; Background of Canvas
(def canvas-colour 0xe7e7e7)

; Number of ms between title screen text slides
(def title-spacing 500)

; The rate by which gravity increases per frame
(def gravity-increase 0.0005)
