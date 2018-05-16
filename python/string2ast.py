import config
from parse import parse
from clojure import *


def xe(e, cte):
    if constExpr(e):
        return xeConstExpr(e, cte)
    if identExpr(e):
        return xeIdentExpr(e, cte)
    if formExpr(e):
        return xeFormExpr(e, cte)
    print ("Syntax error")
    exit()

def xeConstExpr(e, cte):
  return makeLit([], e)

def xeIdentExpr(e, cte):
    b = xeLookup(e, cte)
    if varClj(b):
        return makeRef([], b)
    else:
        print("Can't reference nonvariable")
        exit()

def xeFormExpr (e, cte):
    h = car(e)
    b = identExpr(h) and xeLookup(h, cte)
    if isMacro(b):
        f = macroExpander(b)
        return f(e, cte)
    else:
        return makeApp(xeExprs(e, cte))

def xeExprs(le, cte):
  return [xe(x, cte) for x in le]


def makeInitialCte():

    def binOp(op, n):
        def m(e, cte):
            if len(cdr(e)) == n:
                return makePrim(xeExprs(cdr(e), cte), op)
            else:
                print("Expecting 2 args")
                exit()
        return lambda x, y: m(x, y)

    def setFunc(e, cte):
        if len(cdr(e)) == 2:
            b = xeLookup(cdr(e), [])
            if isVarClj(b):
                return makeSet(xeExprs(cddr(e), cte), b)
            else:
                print("BAD CASE SETFUNC")
                exit()

    def defFunc(e,cte):
        return xe(cons('set!', cdr(e)), cte)

    def ifFunc(e, cte):
        if len(cdr(e)) == 3:
            return makeCnd(xeExprs(cdr(e), cte))
        elif len(cdr(e) == 2):
            return xe(['if', cadr(e), caddr(e), False], cte)
        else:
            print ("BAD IF")
            exit()


    def lambdaFunc(e, cte):
        if len(cdr(e)) >= 1:
            params = [newVar(x) for x in cadr(e)]
            newCte = extendClj(params, cte)
            return makeLam(xe(cons('begin', cddr(e)), newCte), params)
        else:
            print("Lambda expects a parameter")
            exit()


    def beginFunc(e, cte):
        if len(cdr(e))==0:
            return xe(False, cte)
        elif len(cdr(e))==1:
            return xe(cdr(e), cte)
        else:
            return makeSeq(xeExprs(cdr(e), cte))


    def letFunc(e, cte):
        if len(cdr(e))==1:
            return xe(cons(cons('lambda',
                                cons([car(x) for x in cadr(e)],
                                    cddr(e))),
                           [cadr(x) for x in cadr(e)]),
                      cte)
        else:
            print("BAD LET")
            exit()
            
    def orFunc(e, cte):
        if len(cdr(e)) == 0:
            return xe(False, cte)
        elif len(cdr(e)) == 1:
            return xe(cdr(e), cte)
        else:
            x1 = str(cdr(e))
            x2 = ' '.join([str(x) for x in cdr(e)])
            s = ''.join(["((lambda (t1 t2) (if t1 t1 (t2))) ",
                        x1, "(lambda () (or ", x2, ")))"])
            x3 = parse(s)
            return xe(x3, cte)

    def andFunc(e, cte):
        if len(cdr(e)) == 0:
            return xe(True, cte)
        elif len(cdr(e)) == 1:
            return xe(cdr(e), cte)
        else:
            x1 = str(cdr(e))
            x2 = ' '.join([str(x) for x in cdr(e)])
            s = ''.join(["((lambda (t1 t2) (if t1 (t2) t1)) ",
                        x1, "(lambda () (and ", x2, ")))"])
            x3 = parse(s)
            return xe(x3, cte)



    return [
            makeMacro('=', binOp('%=', 2)),
            makeMacro ('<', binOp('%<', 2)),
            makeMacro ('+', binOp('%+', 2)),
            makeMacro ('-', binOp('%-', 2)),
            makeMacro ('*', binOp('%*', 2)),
            makeMacro ('display', binOp('%display', 1)),
            makeMacro ('set!', setFunc),
            makeMacro ('define', defFunc),
            makeMacro ('if', ifFunc),
            makeMacro ('lambda', lambdaFunc),
            makeMacro ('begin', beginFunc),
            makeMacro ('let', letFunc),
            makeMacro ('or', orFunc),
            makeMacro ('and', andFunc)]

def string2list(s):
    sPrime = '(begin {})'.format(s)
    p = parse(sPrime)

# def xeLookupGlobalCte (var):
#   (lookup var @xe-global-cte))
# (defn xe-add-to-global-cte [var]
#   (swap! xe-global-cte (fn [v] (conj v var))))
# 
# (defn xe-lookup [sym cte]
#   (or (lookup sym cte)
#       (xe-lookup-global-cte sym)
#       (let [v (new-global-var sym)]
#         (xe-add-to-global-cte v)
#         v)))
# 
# (swap! xe-global-cte (fn [_] (make-initial-cte)))
# 
# (defn parse-scheme-string [s]
#   (let [
#         s' (str "(" s ")")
#         r  (read-string s')
#         ]
#     (conj r 'begin)))
# 
# (defn parse [input] (xe (parse-scheme-string input) '()))
# 
