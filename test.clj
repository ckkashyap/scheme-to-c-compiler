(use '[compiler.main :as compiler])

(def scheme-string (slurp "test/test.scm"))

(def ast (compiler/string2ast scheme-string))

(def ast-cps-converted (compiler/cps-convert ast))

(def ast-closure-converted (compiler/closure-convert ast-cps-converted))

(spit "test/beforecodegen.scm" ast-closure-converted)

(def code (compiler/codegen ast-closure-converted))


(spit "test/test.c" (code2string code))

(System/exit 0)
