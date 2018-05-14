import sys
from parse import parse
from clojure import *
from clconvert import *
from codegen import codeGenerate, code2string

raw = sys.stdin.read()
parsed = parse(raw)
code = codeGenerate(parsed)

with open("beforeclosure.scm") as f:
    x = f.read()
    p = parse(x)

    c = closureConvert(p)
    
    print("AA" + str(c))


with open("beforecodegen.scm") as f:
    x = f.read()
    p = parse(x)

    print("BB" + str(p))

#print(code)



