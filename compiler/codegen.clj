(ns compiler.codegen
  (:require [clojure.set])
  (:use [compiler.ast])
  (:use [compiler.symbol])  
  (:use [compiler.bridge])
  (:use [compiler.error])  
  (:use [compiler.string2ast :only [xe]]))

; code generation
(defn interval [n m] ; returns the list (n n+1 n+2 ... m)
  (if (<= n m) (cons-clj n (interval (+ n 1) m)) (list)))

(def code-gen)
(def lambda-todo (atom (list)))
(def lambda-count (atom 0))

(defn add-lambda! [lam]
  (let [i @lambda-count]
    (swap! lambda-count #(+ % 1))
    (swap! lambda-todo #(cons-clj (cons-clj i lam) %))
    i))

(defn cg-list [asts vars stack-env sep cont]
  (if (empty? asts)
    (cont "" stack-env)
    (let [x (code-gen (car asts) stack-env)]
      (cg-list (cdr asts)
               (cdr vars)
               (cons-clj (car vars) stack-env)
               sep
               (fn [code stack-env]
                       (cont (list x sep code)
                             stack-env))))))

(defn cg-args [args stack-env]
  (cg-list args
           (interval 1 (count args))
           stack-env
           ""
           (fn [code stack-env]
                   code)))

(def global-vars (atom (list)))

(defn access-var [var stack-env]
  (if (global-var? var)
    (let [i (pos-in-list var @global-vars)]
      (list "GLOBAL(" i "/*" (var-uid var) "*/)"))
    (let [i (- (count stack-env)
                (pos-in-list var stack-env)
                1)]
      (list "LOCAL(" i "/*" (var-uid var) "*/)"))))

(defn cg [stack-env ast]

  (cond
    (lit? ast)
    (let [val (lit-val ast)]
               (case val
                 false (list " PUSH(FALSEOBJ));")
                 true (list " PUSH(TRUEOBJ));")
                 (list " PUSH(INT2OBJ(" val "));")))

    (ref? ast)
    (let [var (ref-var ast)]
      (list " PUSH(" (access-var var stack-env) ");"))

    (set-clj? ast)
    (let [var (set-var ast)]
      (list
       (cg stack-env (car (ast-subx ast)))
       " " (access-var var stack-env) " = TOS();"))

    (cnd? ast)
    (let [x (map (fn [_x] (cg stack-env _x)) (ast-subx ast))]
      (list (car x)
            "\n if (POP()) {\n"
            (cadr x)
            "\n } else {\n"
            (caddr x)
            "\n }"))

    (prim? ast)
    (let [args (ast-subx ast)
          op (prim-op ast)]
      
      (cond

        (= op '%=) (list (cg-args args stack-env) " EQ();")
        (= op '%<) (list (cg-args args stack-env) " LT();")
        (= op '%+) (list (cg-args args stack-env) " ADD();")
        (= op '%-) (list (cg-args args stack-env) " SUB();")
        (= op '%*) (list (cg-args args stack-env) " MUL();")
        (= op '%display) (list (cg-args args stack-env) " DISPLAY();")
        (= op '%halt) (list (cg-args args stack-env) " HALT();")	 

        (= op '%closure)
        (let [i (add-lambda! (car args))
              n (count (cdr args))
              s (list "CLOSURE(" i "," n ");")]
          (list
           (cg-args (cdr args) stack-env)
           " BEGIN_" s
           (map (fn [j]
                        (list " INICLO(" j ");"))
                (reverse (interval 1 n)))
           " END_" s))
      
        (= op '%closure-ref)
        (let [i (lit-val (cadr args))]
          (list
           (cg stack-env (car args))
           " TOS() = CLOSURE_REF(TOS()," i ");"))
      
        :else (error (str "unknown primitive " (= '%closure op)))))

    (app? ast)
    (let [fnc (car (ast-subx ast))
          args (cdr (ast-subx ast))
          n (count args)]
      (if (lam? fnc)
        (cg-list args
                 (lam-params fnc)
                 stack-env
                 "\n"
                 (fn [code new-stack-env]
                         (list
                          code
                          (code-gen (car (ast-subx fnc))
                                    new-stack-env))))
        (cg-list args
                 (interval 1 n)
                 stack-env
                 "\n"
                 (fn [code new-stack-env]
                   (let [start (count stack-env)
                         s (list "JUMP(" n ");")]
                     (list
                      code
                      " BEGIN_" s
                      (map (fn [j]
                             (list " PUSH(LOCAL(" (+ j start) "));"))
                           (interval 0 (- n 1)))
                      " END_" s))))))
  
    (lam? ast)
                                        ; this case is impossible after CPS-conversion
    (list " PUSH(INT2OBJ(" (add-lambda! ast) "));")
  
    (seq? ast)
                                        ; this case is impossible after CPS-conversion
    (map (fn [ast]
           (list (cg stack-env ast) "DROP();"))
         (ast-subx ast))

    :else
    (error (str "unknown ast" ast))))

    
(defn code-gen [ast stack-env]
    (cg stack-env ast))

(defn limit [s n]
  (let [c (count s)
        [i e] (if (> c n) [n "..."] [c ""])
        ]
    (str (subs s 0 i) e)))

(def source)
(defn compile-all-lambdas []
  (if (empty? @lambda-todo)
    ""
    (let [x (car @lambda-todo)
          ast (cdr x)]
      (swap! lambda-todo #(cdr %))
      (list
       "case " (car x) ": /* " (limit (pr-str (source ast)) 57) " */\n\n"
       (code-gen (car (ast-subx ast))
                 (reverse (lam-params ast)))
       "\n\n"
       (compile-all-lambdas)))))

(defn code-generate [ast]
  (let [total (slurp "compiler/runtime.c")
        m (re-matches #"(?s)(.*)//__SCHEME_CODE__.(.*)" total)
        prefix (m 1)
        suffix (m 2)]
   (swap! global-vars (fn [_x] (fv ast)))
   (add-lambda! ast)
   (let [code (compile-all-lambdas)]
       (list
        (list
 	"#define NB_GLOBALS " (count @global-vars) "\n"
 	"#define MAX_STACK " 100 "\n\n" ; could be computed...
 	prefix)
        code
        suffix))))


(defn source [ast]
  (cond (lit? ast)
        (lit-val ast)

        (ref? ast)
        (var-uid (ref-var ast))

        (set-clj? ast)
        (list 'set!
              (var-uid (set-var ast))
              (source (car (ast-subx ast))))

        (cnd? ast)
        (cons 'if (doall (map source (ast-subx ast))))

        (prim? ast)
        (cons (prim-op ast) (doall (map source (ast-subx ast))))

        (app? ast)
        (if (lam? (car (ast-subx ast)))
          (list 'let
                (doall (map (fn [p a]
                              (list (var-uid p) (source a)))
                            (lam-params (car (ast-subx ast)))
                            (cdr (ast-subx ast))))
                (source (car (ast-subx (car (ast-subx ast))))))
          (doall (map source (ast-subx ast))))

        (lam? ast)
        (list 'lambda
              (doall (map var-uid (lam-params ast)))
              (source (car (ast-subx ast))))

        (seq? ast)
        (cons 'begin (map source (ast-subx ast)))

        :else
        (error "unknown ast" ast)))
