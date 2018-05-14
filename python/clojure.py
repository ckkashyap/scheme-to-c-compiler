import sys
import config
from functools import reduce
 
def first(l):
    return l[0]
def rest(l):
    return l[1:]
def car(l):
    return first(l)
def cdr(l):
    return rest(l)
def cddr(l):
    return rest(rest(l))
def cadr(l):
    return first(rest(l))
def caddr(l):
    return (first (rest (rest(l))))
def cadddr (l):
    return first(rest(rest(rest(l))))
def length(x):
    return len(x)
def isNull(x):
    return not x
def cons(v, l):
    nl = list(l)
    nl.insert(0, v)
    return nl
def isList(l):
    return isinstance(l, list)
 
def astSubx(ast):
    return caddr(ast)
 

def booleanClj(e):
    return type(e) == type(True)

def constExpr(e):
   return booleanClj(e) or type(e) == type(1)

def identExpr(e):
    return type(e) == type("")

def formExpr(l):
    return (isinstance(l, list) and len(l) > 0)



def litVal(ast):
    return cadddr(ast)
def isLit(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "AST") and (cadr(ast) == "LIT")
 
def refVar(ast):
    return cadddr(ast)
def isRef(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "AST") and (cadr(ast) == "REF")
 
def setVar(ast):
    return cadddr(ast)

def isSet(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "AST") and (cadr(ast) == "SET")
 
def isCnd(ast):
    return isList(ast) and (len(ast)==3) and (car(ast) == "AST") and (cadr(ast) == "CND")
 
def primOp(ast):
    return cadddr(ast)
def isPrim(ast):
   return isList(ast) and (len(ast)==4) and (car(ast) == "AST") and (cadr(ast) == "PRIM")
 
def isApp(ast):
    return isList(ast) and (len(ast)==3) and (car(ast) == "AST") and (cadr(ast) == "APP")
 
def lamParams(ast):
    return cadddr(ast)
def isLam(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "AST") and (cadr(ast) == "LAM")
 
def isSeq(ast):
    return isList(ast) and (len(ast)==3) and (car(ast) == "AST") and (cadr(ast) == "SEQ")
 
def isMacro(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "BINDING") and (cadr(ast) == "MACRO")

def posInList(x,lst):
    if not lst:
        return False
 
    for i, e in enumerate(lst):
        if e == x:
            return i
 
    return False
 
 
def diff(s1, s2):
    if not s1:
        return []
    if first(s1) in s2:
        return diff(rest(s1), s2)
    else:
        return cons(first(s1), diff(rest(s1), s2))
 
def union(s1,s2):
    if not s1:
        return s2
    if not s2:
        return s1
    if first(s1) in s2:
        return union(rest(s1),s2)
    else:
        return cons(first(s1), union(rest(s1), s2))
 
def unionMulti(ls):
    if not ls:
        return []
    return reduce(union, ls)
 
def fv(ast, ind=0):
    if isRef(ast):
        r = [refVar(ast)]
        return r
    if isSet(ast):
        r = union(fv(first(astSubx(ast)), ind + 1), [setVar(ast)])
        return r
    if isLam(ast):
        r = diff(fv(first(astSubx(ast)), ind + 1), lamParams(ast))
        return r
    r = unionMulti([fv(i, ind + 1) for i in astSubx(ast)])
    return r
 
 
def makeLit (subx, val):
    return ["AST", "LIT", subx, val]

def makeRef (subx, var):
  return ["AST", "REF", subx, var]

def makeSet (subx, var):
  return ["AST", "SET", subx, var]

def makeCnd (subx):
  return ["AST", "CND", subx]

def makePrim (subx, op):
  return ["AST", "PRIM", subx, op]

def makeApp (subx):
  return ["AST", "APP", subx]

def makeLam (subx, params):
  return ["AST", "LAM", subx, params]

def makeSeq (subx):
  return ["AST", "SEQ", subx]

def makeMacro (id, expander):
  return ["BINDING", "MACRO", id, expander]

def makeVar (id, uid):
  return ["BINDING", "VAR", id, uid]


def makeMacro (i, expander):
  return ["BINDING", "MACRO", i, expander]
def isMacro (b):
    return isList(b) and (len(b)==4) and (car(b) == "BINDING") and (cadr(b) == "MACRO")
def macroExpander (b):
    cadddr(b)

def isGlobalVar(v):
    return bindingId(v) == varUid(v)

def bindingId(v):
    return caddr(v)
def varUid(v):
    return cadddr(v)

def isVarClj(ast):
    return isList(ast) and (len(ast)==4) and (car(ast) == "BINDING") and (cadr(ast) == "VAR")
 
def getVarNumber():
    r = config.varNumber
    config.varNumber = r + 1
    return r

def newVar(i):
    return makeVar(i, '{i}.{n}'.format(i=i, n=getVarNumber()))

def extend1 (r, l1, l2):
    c1 = count(l1)
    c2 = count(l2)
    if c1 == 0 and c2 == 0:
        return [i for i in reversed(r)]
    elif c1 > 0:
        return extend1(cons(first(l1), r), rest(l1), l2)
    elif c2 > 0:
        return extend1(cons(first(l2), r), l1, rest(l2))

def extendClj (bindings, env):
  return extend1((list), bindings, env)


def lookup(id, env):
    if len(env) == 0:
        return False

    e = [env[x] for x in env if 

  (cond (empty? env)                     false
        (= (binding-id (car env)) id) (car env)
        :else                            (lookup id (rest env))))
