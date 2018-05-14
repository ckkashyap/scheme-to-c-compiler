import re
from clojure import *

def interval (n, m):
    return range (n, m + 1)
 
lambdaTodo = list()
lambdaCount = 0
globalVars = list()
 
 
def addLambda(lam):
    global lambdaTodo, lambdaCount
    i = lambdaCount
    lambdaTodo.insert(0, cons(i, lam))
    lambdaCount = i + 1
    return i
 
def cgList (asts, vrs, stackEnv, sep, cont):
    if asts:
        x = codeGen(car(asts), stackEnv)
        return cgList(cdr(asts), cdr(vrs), cons(car(vrs), stackEnv), sep, lambda code, sv: cont([x, sep, code], sv))
    else:
        return cont("", stackEnv)
 
def cgArgs (args, stackEnv):
    return cgList(args, interval(1, len(args)), stackEnv, "", lambda code, sv: code)
 
def accessVar(v, stackEnv):
    global globalVars
    if isGlobalVar(v):
        i = posInList(v, globalVars)
        return ["GLOBAL(", i, "/*", varUid(v), "*/)"]
    else:
        i = len(stackEnv) - posInList(v, stackEnv) - 1
        return ["LOCAL(", i, "/*", varUid(v), "*/)"]
 
def cg(sv, ast):
    if isLit(ast):
        val = litVal(ast)
        return [" PUSH(INT2OBJ(", val, "));"]
 
    if isRef(ast):
        var = refVar(ast)
        return [" PUSH(", accessVar(var, sv), ");"]
 
    if isSet(ast):
        var = setVar(ast)
        return [cg(sv, car(astSubx(ast))), " ", accessVar(var, sv), " = TOS();"]
 
    if isCnd(ast):
        x = [cg(sv, i) for i in astSubx(ast)]
        return [car(x), "\n if (POP()) {\n", cadr(x), "\n } else {\n", caddr(x), "\n }"]
 
    if isPrim(ast):
        args = astSubx(ast)
        op = primOp(ast)
        if op == "%=":
            return [cgArgs(args, sv), " EQ();"]
        if op == "%<":
            return [cgArgs(args, sv), " LT();"]
        if op == "%+":
            return [cgArgs(args, sv), " ADD();"]
        if op == "%-":
            return [cgArgs(args, sv), " SUB();"]
        if op == "%*":
            return [cgArgs(args, sv), " MUL();"]
        if op == "%display":
            return [cgArgs(args, sv), " DISPLAY();"]
        if op == "%halt":
            return [cgArgs(args, sv), " HALT();"]
        if op == "%closure":
            i = addLambda(car(args))
            n = len(cdr(args))
            s = ["CLOSURE(", i, ",", n, ");"]
            return [cgArgs(cdr(args), sv), " BEGIN_", s, [[" INICLO(", i, ");"] for i in reversed(interval(1,n))], " END_", s]
        if op == "%closure-ref":
            i = litVal(cadr(args))
            return [cg(sv, car(args)), " TOS() = CLOSURE_REF(TOS(),", i, ");"]
        print("Unknown primitive")
        exit()
 
    if isApp(ast):
        fnc = car(astSubx(ast))
        args = cdr(astSubx(ast))
        n = len(args)
        if isLam(fnc):
            return cgList(args, lamParams(fnc), sv,  "\n", lambda code, newSv:[code, codeGen(car(astSubx(fnc)), newSv)])
        else:
            return cgList(
                    args,
                    interval(1, n),
                    sv,
                    "\n",
                    lambda code, newSv: [code, " BEGIN_JUMP(", n, ");", [[" PUSH(LOCAL(", j + len(sv), "));"] for j in interval(0, n - 1)], " END_JUMP(", n, ");"])
 
    print("BAD CASE")
    exit()
 
def collapse(outList, codeList):
    if codeList and isinstance(codeList, list):
        if len(codeList) > 0:
            return collapse([], first(codeList)) + collapse([], rest(codeList))
        else:
            print("This should not happen")
            exit()
    else:
        x = str(codeList)
        if x == "[]":
            return '' 
        else:
            return x
            
def code2string(codeList):
    return ''.join(collapse([], codeList))

 
def codeGen(ast, sv):
    return cg(sv, ast)
 
def compileAllLambdas():
    global lambdaTodo
    if not lambdaTodo:
        return ""
    else:
        x = car(lambdaTodo)
        ast = cdr(x)
        lambdaTodo = rest(lambdaTodo)
        
        return ["case ", car(x), ":\n", codeGen(car(astSubx(ast)), [i for i in reversed(lamParams(ast))]), "\n\n", compileAllLambdas()]

def codeGenerate(ast):
    global globalVars
    globalVars = fv(ast)
    addLambda(ast)
    code = compileAllLambdas()
    x = "#define NB_GLOBALS " + str(len(globalVars)) + "\n"
    y = "#define MAX_STACK 100 \n\n"

    code = code2string(code)

    with open("compiler/runtime.c") as f: rt = f.read()
    nrt = re.sub("//__SCHEME_CODE__", code, rt);

    return x + y + nrt
