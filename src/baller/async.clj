(ns baller.async
  (:require [cljs.core.async.macros :as m]))

(defmacro <!* [test & body]
  `(let [result# (cljs.core.async/<! ~@body)]
     (if ~test
       result#
       (throw (js/Error "go-while exit")))))

(defmacro >!* [test & body]
  `(let [result# (cljs.core.async/>! ~@body)]
     (if ~test
       result#
       (throw (js/Error "go-while exit")))))

(defn process-body [test [head & tail]]
  (let [[processed-head processed-tail]
          (cond
            (identical? '() head) ['() tail]
            (identical? '[] head) ['[] tail]
            (list? head) [(process-body test head) tail]
            (vector? head) [(vec (process-body test head)) tail]
            (map? head) [(into {} (process-body test (seq head))) tail]
            (= '<! head) ['baller.async/<!* (cons test tail)]
            (= '>! head) ['baller.async/>!* (cons test tail)]
            :default [head tail])]
    (if processed-tail
      (cons processed-head (process-body test processed-tail))
      (list processed-head))))

(defmacro go-while [test & body]
  `(m/go ~@(process-body test body)))
