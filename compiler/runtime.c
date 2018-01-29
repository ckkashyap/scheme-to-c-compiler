#include <stdio.h>
#include <stdlib.h>

#define HEAP_SIZE 1000000

typedef long long obj;

obj global[NB_GLOBALS];
obj stack[MAX_STACK];
obj dingo[HEAP_SIZE];
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


void dump(obj *sp, obj *hp)
{
  int i;
  printf("STACK: ");
  for(i = 0; i < 10; i++)
    {
      printf("%08p ", sp[i]);
    }
  printf("\n");

  printf("HEAP: ");
  for(i = 0; i < 10; i++)
    {
      printf("%08p ", hp[i]);
    }
  printf("\n");

}

obj execute (void)
{
  int pc = 0;
  obj *sp = stack;
  obj *hp = &heap[HEAP_SIZE];
  printf("sp = %08p; hp = %08p\n", sp, hp);
  dump(sp, hp);
  printf("-----\n");


  jump: switch (pc) {

//__SCHEME_CODE__
  }
  return POP();
}

int main () { printf ("result = %d\n", OBJ2INT(execute ())); return 0; }
