(ns compiler.error)

(defn error [& m]
  (throw (Exception. (apply str m))))

