(ns compiler.bridge)

(defn car [l] (first l))
(defn cdr [l] (rest l))
(defn cddr [l] (rest (rest l)))
(defn cadr [l] (first (rest l)))
(defn caddr [l] (first (rest (rest l))))
(defn cadddr [l] (first (rest (rest (rest l)))))
(defn length [x] (count x))
(def null? empty?)


