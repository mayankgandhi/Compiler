#include <stdio.h>
#include <inttypes.h>
int main(int argc, char **argv){
int64_t r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
int64_t reserved = 0, y = 0;
int64_t stack[100];
int64_t *sp = &stack[99];
int64_t *fp = &stack[99];
int64_t *ra = &&exit;
goto mainEntry;
mainEntry:
sp = sp - 2;
*(sp+2) = fp;
*(sp+1) = ra;
fp = sp;
sp = sp - 2;

r1 = 10;
*(fp-1) = r1;
r1 = *(fp-1);
y = r1;
r1 = y;
reserved = r1;
r1 = 2;
*(fp-1) = r1;
r1 = reserved;
r2 = *(fp-1);
r3 = r1 / r2;
*(fp-2) = r3;
r1 = *(fp-2);
reserved = r1;

sp = sp + 2;
fp = *(sp+2);
ra = *(sp+1);
sp = sp + 2;
goto *ra;
exit:
return reserved;
}