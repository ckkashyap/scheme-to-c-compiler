(ns compiler.main
  (:require
   [compiler.string2ast :as string2ast]
   [compiler.cpsconvert :as cpsconvert]
   [compiler.clconvert :as clconvert]
   [compiler.codegen :as codegen]))

(defn string2ast [input]
  (string2ast/parse input))

(defn cps-convert [ast]
  (cpsconvert/cps-convert ast))

(defn closure-convert [ast]
  (clconvert/closure-convert ast))

(defn codegen [ast]
  (codegen/code-generate ast))


(defn collapse [out-list code-list]
  (cond
    (or (seq? code-list) (list? code-list))
    (if (> (count code-list) 0)
      (concat (collapse () (first code-list)) (collapse () (rest code-list))))

    :else
    (list code-list)))
  
(defn code2string [code-list]
  (clojure.string/join "" (collapse () code-list)))

(defn compile2c [f o]
  (spit o (code2string (codegen (closure-convert (cps-convert (string2ast (slurp f))))))))
    
