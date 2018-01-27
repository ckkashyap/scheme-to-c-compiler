(ns compiler.clconvert
  (:use [compiler.symbol])
  (:use [compiler.bridge])
  (:use [compiler.ast])
  (:use [compiler.error])
  (:use [compiler.string2ast :only [ xe ]]))

(defn cc [self-var free-vars ast]
  
  (cond
    (lit? ast)
    ast

    (ref? ast)
    (let [i (pos-in-list (ref-var ast) free-vars)]
      (if i
        (make-prim
         (list (make-ref '() self-var)
               (make-lit '() (+ i 1)))
         '%closure-ref)
        ast))

    (set-clj? ast)
    (make-set (doall (map (fn [x] (cc self-var free-vars x)) (ast-subx ast)))
              (set-var ast))

    (cnd? ast)
    (make-cnd  (doall (map (fn [x] (cc self-var free-vars x)) (ast-subx ast))))

    (prim? ast)
    (make-prim (doall (map (fn [x] (cc self-var free-vars x)) (ast-subx ast)))
                    (prim-op ast))

    (app? ast)
    (let [func (car (ast-subx ast))
          args (doall (map (fn [x] (cc self-var free-vars x)) (cdr (ast-subx ast))))]
      (if (lam? func)
        (make-app
         (cons (make-lam
                    (list ((fn [x] (cc self-var free-vars x)) (car (ast-subx func))))
                    (lam-params func))
                   args))
        (let [f ((fn [x] (cc self-var free-vars x)) func)]
          (make-app
           (cons (make-prim
                      (list f
                            (make-lit '() 0))
                      '%closure-ref)
                     (cons f
                               args))))))
        
    (lam? ast)
    (let [new-free-vars (filter (fn [v] (not (global-var? v))) (fv ast))
          new-self-var (new-var 'self)]
      (make-prim
       (cons (make-lam
                  (list (cc new-self-var new-free-vars (car (ast-subx ast))))
                  (cons new-self-var
                            (lam-params ast)))
                 (doall (map (fn [v]
                                     ((fn [x] (cc self-var free-vars x)) (make-ref '() v)))
                             new-free-vars)))
       '%closure))
        
    (seq? ast)
                                        ; this case is impossible after CPS-conversion
    (make-seq (map (fn [x] (cc self-var free-vars x)) (ast-subx ast)))
    :else (error "unknown ast" ast)))

(defn convert [ast self-var free-vars]
  (cc self-var free-vars ast))

(defn closure-convert [ast]
  (make-lam (list (convert ast false '())) '()))
