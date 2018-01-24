(ns compiler.bridge)

(def line-number (atom 0))
(def indent (atom 0))

(defn debug-indent [] (swap! indent #(+ % 4)))
(defn debug-outdent [] (swap! indent #(- % 4)))

(defn debug-print1 [s]
  (spit "debug.txt" (str @line-number ": " (clojure.string/join (repeat @indent " ")) s "\n") :append true)
  (swap! line-number #(inc %)))

(defn car [l] (first l))
(defn cdr [l] (rest l))
(defn cddr [l] (rest (rest l)))
(defn cadr [l] (first (rest l)))
(defn caddr [l] (first (rest (rest l))))
(defn cadddr [l] (first (rest (rest (rest l)))))
(defn length [x] (count x))
(def null? empty?)


