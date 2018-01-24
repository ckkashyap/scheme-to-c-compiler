#define NB_GLOBALS 6
#define MAX_STACK 100

#include <stdio.h>
#include <stdlib.h>

#define HEAP_SIZE 1000000

typedef int obj;

obj global[NB_GLOBALS];
obj stack[MAX_STACK];
obj heap[HEAP_SIZE];

#define INT2OBJ(n) ((n) << 1)
#define OBJ2INT(o) ((o) >> 1)

#define PTR2OBJ(p) ((obj)(p) + 1)
#define OBJ2PTR(o) ((obj*)((o) - 1))

#define FALSEOBJ INT2OBJ(0)
#define TRUEOBJ INT2OBJ(1)

#define GLOBAL(i) global[i]
#define LOCAL(i) stack[i]
#define CLOSURE_REF(self,i) OBJ2PTR(self)[i]

#define TOS() sp[-1]
#define PUSH(x) *sp++ = x
#define POP() *--sp

#define EQ() { obj y = POP(); TOS() = INT2OBJ(TOS() == y); }
#define LT() { obj y = POP(); TOS() = INT2OBJ(TOS() < y); }
#define ADD() { obj y = POP(); TOS() = TOS() + y; }
#define SUB() { obj y = POP(); TOS() = TOS() - y; }
#define MUL() { obj y = POP(); TOS() = OBJ2INT(TOS()) * y; }
#define DISPLAY() printf ("%d", OBJ2INT(TOS()))
#define HALT() break

#define BEGIN_CLOSURE(label,nbfree) if (hp-(nbfree+1) < heap) hp = gc (sp);
#define INICLO(i) *--hp = POP()
#define END_CLOSURE(label,nbfree) *--hp = label; PUSH(PTR2OBJ(hp));

#define BEGIN_JUMP(nbargs) sp = stack;
#define END_JUMP(nbargs) pc = OBJ2PTR(LOCAL(0))[0]; goto jump;

obj *gc (obj *sp) { exit (1); } /* no GC! */

obj execute (void)
{
  int pc = 0;
  obj *sp = stack;
  obj *hp = &heap[HEAP_SIZE];

  jump: switch (pc) {

case 0: /* (lambda () (let ((r.11 (set! a 456))) (let ((r.12 (set! b... */

 PUSH(INT2OBJ(456)); GLOBAL(5/*a*/) = TOS();
 PUSH(INT2OBJ(21)); GLOBAL(4/*b*/) = TOS();
 PUSH(INT2OBJ(20)); GLOBAL(3/*x*/) = TOS();
 PUSH(GLOBAL(3/*x*/)); GLOBAL(2/*c*/) = TOS();
 PUSH(INT2OBJ(101)); GLOBAL(1/*d*/) = TOS();
 BEGIN_CLOSURE(1,0); END_CLOSURE(1,0);
 PUSH(LOCAL(5/*r.28*/)); GLOBAL(0/*f*/) = TOS();
 PUSH(INT2OBJ(1)); PUSH(INT2OBJ(2)); ADD();
 PUSH(LOCAL(7/*r.17*/)); BEGIN_CLOSURE(2,1); INICLO(1); END_CLOSURE(2,1);
 PUSH(GLOBAL(2/*c*/)); PUSH(INT2OBJ(20)); EQ();
 BEGIN_CLOSURE(3,0); END_CLOSURE(3,0);
 PUSH(LOCAL(9/*r.21*/));
 PUSH(LOCAL(10/*r.22*/));
 PUSH(LOCAL(8/*k.19*/)); BEGIN_CLOSURE(4,1); INICLO(1); END_CLOSURE(4,1);
 PUSH(LOCAL(11/*t1.6*/));
 if (POP()) {
 PUSH(LOCAL(12/*t2.7*/));
 PUSH(LOCAL(13/*k.23*/));
 BEGIN_JUMP(2); PUSH(LOCAL(14)); PUSH(LOCAL(15)); END_JUMP(2);
 } else {
 PUSH(LOCAL(13/*k.23*/));
 PUSH(LOCAL(11/*t1.6*/));
 BEGIN_JUMP(2); PUSH(LOCAL(14)); PUSH(LOCAL(15)); END_JUMP(2);
 }

case 4: /* (lambda (self.47 r.20) (if r.20 ((%closure-ref (%closure-... */

 PUSH(LOCAL(1/*r.20*/));
 if (POP()) {
 PUSH(LOCAL(0/*self.47*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(INT2OBJ(50));
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);
 } else {
 PUSH(LOCAL(0/*self.47*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(INT2OBJ(10));
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);
 }

case 3: /* (lambda (self.45 k.24) (let ((r.25 (%= b 21))) (let ((r.2... */

 PUSH(GLOBAL(4/*b*/)); PUSH(INT2OBJ(21)); EQ();
 BEGIN_CLOSURE(5,0); END_CLOSURE(5,0);
 PUSH(LOCAL(2/*r.25*/));
 PUSH(LOCAL(3/*r.26*/));
 PUSH(LOCAL(4/*t1.8*/));
 if (POP()) {
 PUSH(LOCAL(5/*t2.9*/));
 PUSH(LOCAL(1/*k.24*/));
 BEGIN_JUMP(2); PUSH(LOCAL(6)); PUSH(LOCAL(7)); END_JUMP(2);
 } else {
 PUSH(LOCAL(1/*k.24*/));
 PUSH(LOCAL(4/*t1.8*/));
 BEGIN_JUMP(2); PUSH(LOCAL(6)); PUSH(LOCAL(7)); END_JUMP(2);
 }

case 5: /* (lambda (self.46 k.27) ((%closure-ref k.27 0) k.27 (%= d ... */

 PUSH(LOCAL(1/*k.27*/));
 PUSH(GLOBAL(1/*d*/)); PUSH(INT2OBJ(100)); EQ();
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);

case 2: /* (lambda (self.43 r.18) ((%closure-ref f 0) f (%closure (l... */

 PUSH(GLOBAL(0/*f*/));
 BEGIN_CLOSURE(6,0); END_CLOSURE(6,0);
 PUSH(LOCAL(0/*self.43*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(LOCAL(1/*r.18*/));
 BEGIN_JUMP(4); PUSH(LOCAL(2)); PUSH(LOCAL(3)); PUSH(LOCAL(4)); PUSH(LOCAL(5)); END_JUMP(4);

case 6: /* (lambda (self.44 r.10) (%halt r.10)) */

 PUSH(LOCAL(1/*r.10*/)); HALT();

case 1: /* (lambda (self.39 k.29 x.1 y.2) (let ((k.33 (%closure (lam... */

 PUSH(LOCAL(1/*k.29*/)); PUSH(LOCAL(3/*y.2*/)); BEGIN_CLOSURE(7,2); INICLO(2); INICLO(1); END_CLOSURE(7,2);
 PUSH(LOCAL(2/*x.1*/)); PUSH(INT2OBJ(3)); EQ();
 PUSH(LOCAL(2/*x.1*/)); BEGIN_CLOSURE(8,1); INICLO(1); END_CLOSURE(8,1);
 PUSH(LOCAL(5/*r.35*/));
 PUSH(LOCAL(6/*r.36*/));
 PUSH(LOCAL(4/*k.33*/)); BEGIN_CLOSURE(9,1); INICLO(1); END_CLOSURE(9,1);
 PUSH(LOCAL(7/*t1.3*/));
 if (POP()) {
 PUSH(LOCAL(9/*k.37*/));
 PUSH(LOCAL(7/*t1.3*/));
 BEGIN_JUMP(2); PUSH(LOCAL(10)); PUSH(LOCAL(11)); END_JUMP(2);
 } else {
 PUSH(LOCAL(8/*t2.4*/));
 PUSH(LOCAL(9/*k.37*/));
 BEGIN_JUMP(2); PUSH(LOCAL(10)); PUSH(LOCAL(11)); END_JUMP(2);
 }

case 9: /* (lambda (self.42 r.34) (if r.34 ((%closure-ref (%closure-... */

 PUSH(LOCAL(1/*r.34*/));
 if (POP()) {
 PUSH(LOCAL(0/*self.42*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(INT2OBJ(100));
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);
 } else {
 PUSH(LOCAL(0/*self.42*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(INT2OBJ(200));
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);
 }

case 8: /* (lambda (self.41 k.38) ((%closure-ref k.38 0) k.38 (%= (%... */

 PUSH(LOCAL(1/*k.38*/));
 PUSH(LOCAL(0/*self.41*/)); TOS() = CLOSURE_REF(TOS(),1); PUSH(INT2OBJ(4)); EQ();
 BEGIN_JUMP(2); PUSH(LOCAL(2)); PUSH(LOCAL(3)); END_JUMP(2);

case 7: /* (lambda (self.40 r.30) (let ((k.5 20)) (let ((r.32 (%+ a ... */

 PUSH(INT2OBJ(20));
 PUSH(GLOBAL(5/*a*/)); PUSH(LOCAL(2/*k.5*/)); ADD();
 PUSH(LOCAL(0/*self.40*/)); TOS() = CLOSURE_REF(TOS(),2); PUSH(LOCAL(3/*r.32*/)); ADD();
 PUSH(LOCAL(0/*self.40*/)); TOS() = CLOSURE_REF(TOS(),1);
 PUSH(LOCAL(1/*r.30*/)); PUSH(LOCAL(4/*r.31*/)); ADD();
 BEGIN_JUMP(2); PUSH(LOCAL(5)); PUSH(LOCAL(6)); END_JUMP(2);

  }
  return POP();
}

int main () { printf ("result = %d\n", OBJ2INT(execute ())); return 0; }
