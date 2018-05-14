from parse import parse


with open("test/test.scm") as f:
    s = f.read()
    s = "(begin " + s  + ")"
    p = parse(s)
    print(p)
    q = parse(str(s))
    print(q)
    if p == q:
        print("GOOD")

