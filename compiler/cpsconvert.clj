(ns compiler.cpsconvert
  (:use [compiler.symbol])
  (:use [compiler.bridge])
  (:use [compiler.ast])
  (:use [compiler.error])
  (:use [compiler.string2ast :only [ xe]]))

(defn cps-convert [ast]

  (def cps-list)
  (def cps-seq)  
  
  (defn cps [ast cont-ast]

    (cond (lit? ast)
          (make-app (list cont-ast ast))
          
          (ref? ast)
          (make-app (list cont-ast ast))
          
          (set-clj? ast)
          (cps-list (ast-subx ast)
                    (fn [val]
                      (make-app
                       (list cont-ast
                             (make-set val
                                       (set-var ast))))))
          
          (cnd? ast)
          (let [xform
                (fn [cont-ast]
                  (cps-list (list (car (ast-subx ast)))
                            (fn [test]
                              (make-cnd
                               (list (car test)
                                     (cps (cadr (ast-subx ast))
                                          cont-ast)
                                     (cps (caddr (ast-subx ast))
                                          cont-ast))))))]
            (if (ref? cont-ast) ; prevent combinatorial explosion
              (xform cont-ast)
              (let [k (new-var 'k)]
                (make-app
                 (list (make-lam
                        (list (xform (make-ref '() k)))
                        (list k))
                       cont-ast)))))
          
          (prim? ast)
          (cps-list (ast-subx ast)
                    (fn [args]
                      (make-app
                       (list cont-ast
                             (make-prim args
                                        (prim-op ast))))))
          
          (app? ast)
          (let [func (car (ast-subx ast))]
            (if (lam? func)
              (cps-list (cdr (ast-subx ast))
                        (fn [vals]
                          (make-app
                           (cons (make-lam
                                  (list (cps-seq (ast-subx func)
                                                 cont-ast))
                                  (lam-params func))
                                 vals))))
              (cps-list (ast-subx ast)
                        (fn [args]
                          (make-app
                           (cons (car args)
                                 (cons cont-ast
                                       (cdr args))))))))
          
          (lam? ast)
          (let [k (new-var 'k)]
            (make-app
             (list cont-ast
                   (make-lam
                    (list (cps-seq (ast-subx ast)
                                   (make-ref '() k)))
                    (cons k (lam-params ast))))))
          
          (seq-clj? ast)
          (cps-seq (ast-subx ast) cont-ast)
          
          :else
          (error "unknown ast" ast)))
  
  (defn cps-list [asts inner]
    
    (defn body [x]
      (cps-list (cdr asts)
                (fn [new-asts]
                        (inner (cons x new-asts)))))
    
    (cond (null? asts)
          (inner '())
          
          (or (lit? (car asts))
              (ref? (car asts)))
          (body (car asts))
          
          :else
          (let [r (new-var 'r)]
            (cps (car asts)
                 (make-lam (list (body (make-ref '() r)))
                           (list r))))))
  
  (defn cps-seq [asts cont-ast]
    (cond (null? asts)
          (make-app (list cont-ast false))
          (null? (cdr asts))
          (cps (car asts) cont-ast)
          :else
          (let [r (new-var 'r)]
            (cps (car asts)
                 (make-lam
                  (list (cps-seq (cdr asts) cont-ast))
                  (list r))))))
  
  (let [ast-cps
        (cps ast
             (let [r (new-var 'r)]
               (make-lam
                (list (make-prim (list (make-ref '() r))
                                 '%halt))
                (list r))))]
    (if (lookup 'call/cc (fv ast))
                                        ; add this definition for call/cc if call/cc is needed
      (make-app
       (list (make-lam
              (list ast-cps)
              (list (new-var '_)))
             (xe '(set! call/cc
                        (fn [k f]
                          (f k (fn [_ result] (k result)))))
                 '())))
      ast-cps)))

