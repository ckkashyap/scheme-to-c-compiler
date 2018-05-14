from clojure import *

def cc (selfVar, freeVars, ast, indent=0):
    if isLit(ast):
        print ("{tab}LIT INPUT = {ast}".format(tab=" "*indent, ast=ast))
        print ("{tab}OUTPUT = {ast}".format(tab=" "*indent, ast=ast))
        return ast

    if isRef(ast):
        i = posInList(refVar(ast), freeVars)
        if i:
            print ("{tab}REF INPUT = {ast}".format(tab=" "*indent, ast=ast))
            result =  makePrim([makeRef([], selfVar), makeLit([], i + 1)], '%closure-ref')
            print ("{tab}OUTPUT = {result}".format(tab=" "*indent, result=result))
            return result
        else:
            print ("{tab}REF2 INPUT = {ast}".format(tab=" "*indent, ast=ast))
            print ("{tab}OUTPUT = {result}".format(tab=" "*indent, result=ast))
            return ast

    if isSet(ast):
        print ("{tab}Set INPUT = {ast}".format(tab=" "*indent, ast=ast))
        result = makeSet([cc(selfVar, freeVars, x, indent + 2) for x in astSubx(ast)], setVar(ast))
        print ("{tab}OUTPUT = {ast}".format(tab=" "*indent, ast=result))
        return result

    if isCnd(ast):
        print ("{tab}CND INPUT = {ast}".format(tab=" "*indent, ast=ast))
        result = makeCnd([cc(selfVar, freeVars, x, indent + 2) for x in astSubx(ast)])
        print ("{tab}OUTPUT = {ast}".format(tab=" "*indent, ast=result))
        return result


    if isPrim(ast):
        print ("{tab}PRIM INPUT = {ast}".format(tab=" "*indent, ast=ast))
        result =  makePrim([cc(selfVar, freeVars, x, indent + 2) for x in astSubx(ast)], primOp(ast))
        print ("{tab}OUTPUT = {ast}".format(tab=" "*indent, ast=result))
        return result

    if isApp(ast):
        print ("{tab}APP INPUT = {ast}".format(tab=" "*indent, ast=ast))
        func = car(astSubx(ast))
        args = [cc(selfVar, freeVars, x, indent + 2) for x in cdr(astSubx(ast))]
        if isLam(func):
            print ("LAM")
            result = makeApp(
                    cons(
                        makeLam(
                            [cc(selfVar, freeVars, car(astSubx(func)), indent + 2)],
                            lamParams(func)),
                        args))
            print ("{tab}OUTPUT1 = {ast}".format(tab=" "*indent, ast=result))
            return result
        else:
            print ("NOT LAM")
            f = cc(selfVar, freeVars, func, indent + 2)
            result = makeApp(cons(makePrim([f, makeLit([], 0)], '%closure-ref'), cons(f, args)))
            print ("{tab}OUTPUT2 = {ast}".format(tab=" "*indent, ast=result))
            return result

        
    if isLam(ast):
        print ("{tab}LAM INPUT = {ast}".format(tab=" "*indent, ast=ast))
        newFreeVars = [i for i in fv(ast) if not isGlobalVar(i)]
        newSelfVar = newVar('self')
        result = makePrim(
                cons(
                    makeLam(
                        [cc(newSelfVar, newFreeVars, car(astSubx(ast)), indent + 2)],
                        cons(newSelfVar, lamParams(ast))),
                    [cc(selfVar, freeVars, makeRef([], v), indent + 2) for v in newFreeVars]),
                '%closure')
        print ("{tab}OUTPUT = {ast}".format(tab=" "*indent, ast=result))
        return result

    if isSeq(ast):
        print("This should not have happened")
        exit()
    print("unknown ast {ast}".format(ast=ast))

def convert (ast, selfVar, freeVars):
  return cc(selfVar, freeVars, ast)

def closureConvert (ast):
  return makeLam([convert(ast, False, [])], [])

