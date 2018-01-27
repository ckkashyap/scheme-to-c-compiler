(ns compiler.debug)

(def line-number (atom 0))
(def indent (atom 0))

(defn debug-indent [] (swap! indent #(+ % 4)))
(defn debug-outdent [] (swap! indent #(- % 4)))

(defn debug-print [s]
  (spit
   "debug.txt"
   (str
    @line-number
    ": "
    (clojure.string/join (repeat @indent " ")) s "\n")
   :append true)
  (swap! line-number #(inc %)))
