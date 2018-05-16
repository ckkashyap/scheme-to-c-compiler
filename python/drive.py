import sys
import re
from parse import parse
from clojure import *
from string2ast import *
#from clconvert import *
from codegen import codeGenerate, code2string


def py2clj(s):
    s1 = re.sub(r'\[', '(', s)
    s2 = re.sub(r']', ')', s1)
    s3 = re.sub(r'\'', '', s2)
    s4 = re.sub(r',', '', s3)
    return s4


with open("test/beforecodegen.scm") as f:
	raw = f.read()
	parsed = parse(raw)
	code = codeGenerate(parsed)
	with open('test/python.c', 'w') as o:
		o.write(code)



with open("test/test.scm") as f:
    raw = f.read()
    p = parse('(begin {})'.format(raw))
    with open('test/python-parsed.txt', 'w') as o:
        o.write(py2clj(str(p)))
