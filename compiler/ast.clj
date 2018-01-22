(ns compiler.ast
  (:use [compiler.bridge]))

(def BINDING "BINDING")
(def VAR "VAR")
(def MACRO "MACRO")

(def AST "AST")
(def LIT "LIT")
(def REF "REF")
(def SET "SET")
(def CND "CND")
(def PRIM "PRIM")
(def APP "APP")
(def LAM "LAM")
(def SEQ "SEQ")

(defn ast-subx [x] (caddr x))

(defn make-lit [subx val]
  (list AST LIT subx val))
(defn lit-val [x] (cadddr x))
(defn lit? [x]
  (and
   (list? x)
   (= 4 (count x))
   (= AST (car x))
   (= LIT (cadr x))
   true))

(defn make-ref [subx var]
  (list AST REF subx var))
(defn ref-var [x] (cadddr x))
(defn ref? [x]
  (and
   (list? x)
   (= 4 (count x))
   (= AST (car x))
   (= REF (cadr x))
   true))

(defn make-set [subx var]
  (list AST SET subx var))
(defn set-var [x] (cadddr x))
(defn set-clj? [x]
  (and
   (list? x)
   (= 4 (count x))
   (= AST (car x))
   (= SET (cadr x))
   true))


(defn make-cnd [subx]
  (list AST CND subx))
(defn cnd? [x]
  (and
   (list? x)
   (= 3 (count x))
   (= AST (car x))
   (= CND (cadr x))
   true))

(defn make-prim [subx op]
  (list AST PRIM subx op))
(defn prim? [x]
  (and
   (list? x)
   (= 4 (count x))
   (= AST (car x))
   (= PRIM (cadr x))
   true))
(defn prim-op [x] (cadddr x))

(defn make-app [subx]
  (list AST APP subx))
(defn app? [x]
  (and
   (list? x)
   (= 3 (count x))
   (= AST (car x))
   (= APP (cadr x))
   true))

(defn make-lam [subx params]
  (list AST LAM subx params))
(defn lam-params [x] (cadddr x))
(defn lam? [x]
  (and
   (list? x)
   (= 4 (count x))
   (= AST (car x))
   (= LAM (cadr x))
   true))

(defn make-seq [subx]
  (list AST SEQ subx))
(defn seq-clj? [x]
  (and
   (list? x)
   (= 3 (count x))
   (= AST (car x))
   (= SEQ (cadr x))
   true))

(defn make-macro [id expander]
  (list BINDING MACRO id expander))
(defn macro? [b]
  (and
   (list? b)
   (= 4 (count b))
   (= BINDING (car b))
   (= MACRO (cadr b))
   true))
(defn macro-expander [b] (cadddr b))

(defn make-var [id uid]
  (list BINDING VAR id uid))
(defn var-uid [b] (cadddr b))
(defn var-clj? [b]
  (and
   (list? b)
   (= 4 (count b))
   (= BINDING (car b))
   (= VAR (cadr b))
   true))

(defn boolean-clj? [e] (or (true? e) (false? e)))
(defn const-expr? [e] (or (boolean-clj? e) (number? e)))
(defn ident-expr? [e] (symbol? e))
(defn form-expr? [e]  (and (seq? e) (not (empty? e))))
