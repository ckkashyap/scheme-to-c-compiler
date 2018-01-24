(ns compiler.string2ast
  (:use [compiler.bridge])
  (:use [compiler.error])
  (:use [compiler.ast])
  (:use [compiler.symbol]))

(def xe-const-expr)
(def xe-ident-expr)
(def xe-form-expr)
(def xe-lookup)
(def xe-exprs)


(defn xe [e cte]
  (cond (const-expr? e) (xe-const-expr e cte)
        (ident-expr? e) (xe-ident-expr e cte)
        (form-expr? e)  (xe-form-expr e cte)
        :else            (error "syntax-error" e)))


(defn xe-const-expr [e cte]
  (make-lit '() e))

(defn xe-ident-expr [e cte]
  (let [b (xe-lookup e cte)]
    (if (var-clj? b)
        (make-ref '() b)
        (error "can't reference a nonvariable" e))))

(defn xe-form-expr [e cte]
  (let [h (car e)]
    (let [b (and (ident-expr? h) (xe-lookup h cte))]
      (if (macro? b)
          ((macro-expander b) e cte)
          (make-app (xe-exprs e cte))))))


(defn xe-exprs [le cte]
  (doall (map (fn [x] (xe x cte)) le)))

(defn make-initial-cte []
  (list

   (make-macro '=   ; could have used %= instead
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (make-prim (xe-exprs (cdr e) cte) '%=)
           (error "= expects 2 args"))))

   (make-macro '<   ; could have used %< instead
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (make-prim (xe-exprs (cdr e) cte) '%<)
           (error "< expects 2 args"))))

   (make-macro '+   ; could have used %+ instead
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (make-prim (xe-exprs (cdr e) cte) '%+)
           (error "+ expects 2 args"))))

   (make-macro '-   ; could have used %- instead
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (make-prim (xe-exprs (cdr e) cte) '%-)
           (error "- expects 2 args"))))

   (make-macro '*   ; could have used %* instead
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (make-prim (xe-exprs (cdr e) cte) '%*)
           (error "* expects 2 args"))))

   (make-macro 'display   ; could have used %display instead
     (fn [e cte]
       (if (= (length (cdr e)) 1)
           (make-prim (xe-exprs (cdr e) cte) '%display)
           (error "display expects 1 arg"))))

   (make-macro 'set!
     (fn [e cte]
       (if (= (length (cdr e)) 2)
           (let [b (xe-lookup (cadr e) '())]
             (cond (var-clj? b) (make-set (xe-exprs (cddr e) cte) b)
                   :else (error "can't set! a nonvariable" e)))
           (error "set! expects 2 args"))))

   (make-macro 'define
     (fn [e cte]
       (xe (cons 'set! (cdr e)) cte)))

   (make-macro 'if
     (fn [e cte]
       (cond
         (= (length (cdr e)) 3) (make-cnd (xe-exprs (cdr e) cte))
         (= (length (cdr e)) 2) (xe (list 'if (cadr e) (caddr e) false) cte)
         :else (error "if expects 2 or 3 args"))))

   (make-macro 'lambda
     (fn [e cte]
       (if (>= (length (cdr e)) 1)
           (let [params (map new-var (cadr e))]
             (let [new-cte (extend-clj params cte)]
               (make-lam
                (list (xe (cons 'begin (cddr e)) new-cte))
                params)))
           (error "lambda expects a parameter list"))))

   (make-macro 'begin
     (fn [e cte]
       (cond (= (length (cdr e)) 0) (xe false cte)
             (= (length (cdr e)) 1) (xe (cadr e) cte)
             :else (make-seq (xe-exprs (cdr e) cte)))))

   (make-macro 'let
     (fn [e cte]
       (if (>= (length (cdr e)) 1)
           (xe (cons (cons 'lambda
                           (cons (map car (cadr e))
                                 (cddr e)))
                     (map cadr (cadr e)))
               cte)
           (error "let expects a binding list"))))

   (make-macro 'or
               (fn [e cte]
                 (cond (= (length (cdr e)) 0) (xe false cte)
                       (= (length (cdr e)) 1) (xe (cadr e) cte)
                       :else
                       (let [x1 (pr-str (cadr e))
                             x2 (clojure.string/join " " (map pr-str (cddr e)))
                             s (str "((lambda (t1 t2) (if t1 t1 (t2))) " x1
                                    "(lambda () (or " x2 ")))" )
                             x3 (read-string s)]
                         (xe x3 cte)))))

   (make-macro 'and
               (fn [e cte]
                 (cond (= (length (cdr e)) 0) (xe true cte)
                       (= (length (cdr e)) 1) (xe (cadr e) cte)
                       :else
                       (let [x1 (pr-str (cadr e))
                             x2 (clojure.string/join " " (map pr-str (cddr e)))
                             s (str "((lambda (t1 t2) (if t1 (t2) t1)) " x1
                                    "(lambda () (and " x2 ")))" )
                             x3 (read-string s)]
                         (xe x3 cte)))))
   
  ))

(def xe-global-cte (atom ()))
(defn xe-lookup-global-cte [var]
  (lookup var @xe-global-cte))
(defn xe-add-to-global-cte [var]
  (swap! xe-global-cte (fn [v] (conj v var))))

(defn xe-lookup [sym cte]
  (or (lookup sym cte)
      (xe-lookup-global-cte sym)
      (let [v (new-global-var sym)]
        (xe-add-to-global-cte v)
        v)))

(swap! xe-global-cte (fn [_] (make-initial-cte)))

(defn parse-scheme-string [s]
  (let [
        s' (str "(" s ")")
        r  (read-string s')
        ]
    (conj r 'begin)))

(defn parse [input] (xe (parse-scheme-string input) '()))

