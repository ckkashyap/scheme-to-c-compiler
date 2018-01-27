(ns compiler.symbol
  (:use [compiler.bridge])
  (:use [compiler.ast]))

(defn binding-id [b] (caddr b))

(def var-number (atom 0))
(defn increment-id [a]
  (let [_ (swap! a inc)]
    @a))
(defn get-var-number []
  (increment-id var-number))

(defn union [s1 s2]
  (cond (empty? s1)  s2
        (contains? (into #{} s2) (car s1)) (union (cdr s1) s2)        
        :else (cons (car s1) (union (cdr s1) s2))))

(defn diff [s1 s2]
  (cond (empty? s1)  (list)
        (contains? (into #{} s2) (car s1)) (diff (cdr s1) s2)
        :else (cons (car s1) (diff (cdr s1) s2))))

(defn union-multi [ls]
  (reduce union (list) ls))

(defn fv [ast]
  (let [fv-result
        (cond
          (ref? ast) (list (ref-var ast))
          (set-clj? ast) (union (fv (first (ast-subx ast))) (list (set-var ast)))
          (lam? ast) (diff (fv (first (ast-subx ast))) (lam-params ast))
          :else (union-multi (map fv (ast-subx ast))))
        ]
    fv-result))

(defn pos-in-list [x lst]
  (let [result 
        (loop [l lst i 0]
          (cond
            (empty? l) false
            (not (seq? l)) false
            (= (first l) x) i
            :else (recur (rest l) (inc i))))
        ] result))

(defn new-var [id]
  (make-var
   id
   (symbol (str id "." (get-var-number)))))

(defn new-global-var [id]
  (make-var id id))

(defn global-var? [var]
  (= (binding-id var) (var-uid var)))

(defn extend1 [r l1 l2]
  (let [c1 (count l1)
	c2 (count l2)]
    (cond
      (and (= c1 0) (= c2 0)) (reverse r)
      (> c1 0) (extend1 (conj r (first l1)) (rest l1) l2)
      (> c2 0) (extend1 (conj r (first l2)) l1 (rest l2)))))

(defn extend-clj [bindings env]
  (extend1 (list) bindings env))

(defn lookup [id env]
  (cond (empty? env)                     false
        (= (binding-id (car env)) id) (car env)
        :else                            (lookup id (rest env))))
