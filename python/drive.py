import sys
from parse import parse
from clojure import *
#from clconvert import *
from codegen import codeGenerate, code2string

with open("test/beforecodegen.scm") as f:
	raw = f.read()
	parsed = parse(raw)
	code = codeGenerate(parsed)
	with open('test/python.c', 'w') as o:
		o.write(code)




