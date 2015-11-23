#include "libregexp/regexp9.h"
#include <stdlib.h>
#include <string.h>
#include <setjmp.h>
#include <stdio.h>
#include <ctype.h>
#include <errno.h>
#include <math.h>

//typedefs/aliases
#define R return
#define BK break
#define BZ 1024
typedef void V;
typedef V*P;
typedef double F;
typedef FILE* FP;
typedef size_t L;
typedef char C;
typedef char*S;
typedef int I;

#ifdef IDE
//for the web ide,the stack is printed to standard error
//that way, the Python server can separate the stack from the input
#define SF stderr
#else
#define SF stdout
#endif

I ln,col; //line,col
I isrepl=0;jmp_buf jb; //repl(implies jump on error)?,jump buffer

V em(S s){fprintf(stderr,"\nError @%d:%d: %s\n",ln,col,s);} //error message
V ex(S s){em(s);if(isrepl)longjmp(jb,1);else exit(EXIT_FAILURE);} //error and exit
#define TE ex("wrong type") //type error
#define PE ex("can't parse") //parse error
#define PXE ex(strerror(errno))
P alc(L z){P r;if(!(r=malloc(z)))ex("memory");R r;} //allocate memory
P rlc(P p,L z){P r;if(!(r=realloc(p,z)))ex("memory");R r;} //realloc memory
#define DL(x) free(x)

S rdln(){L z;S r=alc(BZ);if(!fgets(r,BZ,stdin)){if(feof(stdin)){*r=0;R r;}else PXE;}z=strlen(r);if(r[z-1]=='\n')r[z-1]=0;if(z>1&&r[z-2]=='\r')r[z-2]=0;R r;} //read line(XXX:only allows BZ as max length!)
F rdlnd(){F r;S s=rdln();r=strtod(s,0);DL(s);R r;} //read number(should this error on wrong input?)

//stack
typedef struct{P*st;L p,l;}STB;typedef STB*ST; //type:stack,top,len
ST newst(L z){ST s=alc(sizeof(STB));s->st=alc(z*sizeof(P));s->p=0;s->l=z;R s;} //new
V psh(ST s,P x){if(s->p+1==s->l)ex("overflow");s->st[s->p++]=x;} //push
P pop(ST s){if(s->p==0)ex("underflow");R s->st[--s->p];} //pop
P top(ST s){if(s->p==0)ex("underflow");R s->st[s->p-1];} //top
V swp(ST s){P a,b;a=pop(s);b=pop(s);psh(s,a);psh(s,b);} //swap
V rot(ST s){P a,b,c;a=pop(s);b=pop(s);c=pop(s);psh(s,b);psh(s,a);psh(s,c);} //rotate 3
L len(ST s){R s->p;}
V dls(ST s){DL(s->st);DL(s);} //delete
V rev(ST s){P t;L i;for(i=0;i<s->p/2;++i){t=s->st[i];s->st[i]=s->st[s->p-i-1];s->st[s->p-i-1]=t;}} //reverse

ST rst=0; //root stack

//objects
typedef enum{TD,TS,TA,TCB}OT; //decimal,string,array,codeblock
typedef struct{OT t;union{F d;struct{S s;L z;}s;ST a;};}OB;typedef OB*O; //type:type flag,value{decimal,{string,len},array}(NOTE:code blocks use string struct to store their code!)
S tos(O o){
    S r,t;switch(o->t){
    case TD:r=alc(BZ)/*hope this is big enough!*/;if(o->d==(I)o->d)sprintf(r,"%d",(I)o->d);else{L z;sprintf(r,"%f",o->d);z=strlen(r)-1;while(r[z]=='0')r[z--]=0;}BK;
    case TS:r=alc(o->s.z+1);memcpy(r,o->s.s,o->s.z);r[o->s.z]=0;BK;
    case TA:r=alc(BZ)/*XXX:overflow potential here!again!*/;r[0]='[';r[1]=0;I l=len(o->a);if(l){I i;for(i=0;i<l;++i){
        if(i)strcat(r,",");t=tos(o->a->st[i]);strcat(r,t);DL(t);
    }}strcat(r,"]");BK;
    case TCB:r=alc(o->s.z+3);r[0]='{';memcpy(r+1,o->s.s,o->s.z);memcpy(r+1+o->s.z,"}",2);BK;
    }R r;
} //tostring (copies)
O newo(){R alc(sizeof(OB));} //new object
O newod(F d){O r=newo();r->t=TD;r->d=d;R r;} //new object decimal
O newocb(S s,L z){O r=newo();r->t=TCB;r->s.s=alc(z+1);memcpy(r->s.s,s,z);r->s.s[z]=0;r->s.z=z;R r;} //new object code block (copies)
O newocbk(S s,L z){O r=newo();r->t=TCB;r->s.s=s;r->s.z=z;R r;} //new object string (doesn't copy)
O newos(S s,L z){O r=newo();r->t=TS;r->s.s=alc(z+1);memcpy(r->s.s,s,z);r->s.s[z]=0;r->s.z=z;R r;} //new object string (copies)
O newosk(S s,L z){O r=newo();r->t=TS;r->s.s=s;r->s.z=z;R r;} //new object string (doesn't copy)
O newosz(S s){R newos(s,strlen(s));} //new object string w/o len (copies)
O newoskz(S s){R newosk(s,strlen(s));} //new object string w/o len (doesn't copy)
O newoa(ST a){O r=newo();r->t=TA;r->a=a;R r;} //new object array
V dlo(O o){
    switch(o->t){
    case TS:case TCB:DL(o->s.s);BK;
    case TA:while(len(o->a))dlo(pop(o->a));dls(o->a);BK;
    case TD:BK;
    }DL(o);
} //delete object
O toso(O o){S s=tos(o);O r=newosz(s);DL(s);R r;} //wrap tostring in object
O dup(O);O dupa(O o){ST s=newst(BZ);L i=0;for(i=0;i<len(o->a);++i)psh(s,dup(o->a->st[i]));R newoa(s);} //dup array
O dup(O o){
    L z;S s;switch(o->t){
    case TCB:R newocb(o->s.s,o->s.z);BK;
    case TS:R newos(o->s.s,o->s.z);BK;
    case TD:R newod(o->d);BK;
    case TA:R dupa(o);BK;
    }R 0; //appease the compiler
} //dup
O tosocb(O o){if(o->t==TCB){O r=dup(o);r->t=TS;R r;}else R toso(o);} //wrap tostring in object,but return codeblock string form without braces
I eqo(O a,O b){
    if(a->t!=b->t)R 0;
    switch(a->t){
    case TS:case TCB:R a->s.z!=b->s.z?0:memcmp(a->s.s,b->s.s,a->s.z)==0;
    case TD:R a->d==b->d;
    default:ex("non-TS-TD in eqo");R 0;
    }
} //equal
I truth(O o){
    switch(o->t){
    case TD:R o->d!=0;BK;
    case TS:case TCB:R o->s.z!=0;BK;
    case TA:R len(o->a)!=0;BK;
    }
} // is truthy?

//stack-object manips(obj args are freed by caller)
typedef O(*OTB)(O); //single-arg function spec type
typedef O(*OTF)(O,O); //function spec type (e.g. adds, addd, etc.)
typedef V(*OTS)(O,O,ST); //another function spec type (for mul,div)
V gnop(ST,OTF*,I);
O opa(O o,OTF*ft,I e){while(len(o->a)>1)gnop(o->a,ft,e);R dup(top(o->a));} //apply op to array elements

O adds(O a,O b){S rs=alc(a->s.z+b->s.z+1);memcpy(rs,a->s.s,a->s.z);memcpy(rs+a->s.z,b->s.s,b->s.z+1);R newosk(rs,a->s.z+b->s.z);} //add strings
O addd(O a,O b){R newod(a->d+b->d);} //add decimal
OTF addf[]={addd,adds};

O subs(O a,O b){L i,z=a->s.z;S r,p;if(b->s.z==0)R dup(a);for(i=0;i<a->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0)z-=b->s.z;p=r=alc(z+1);for(i=0;i<a->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0)i+=b->s.z-1;else*p++=a->s.s[i];R newosk(r,z);} //sub strings
O subd(O a,O b){R newod(a->d-b->d);} //sub decimal
OTF subf[]={subd,subs};

O lts(O a,O b){R newod(strstr(a->s.s,b->s.s)!=0);}
O ltd(O a,O b){R newod(a->d<b->d);}
OTF ltf[]={ltd,lts};

O gts(O a,O b){R newod(strstr(b->s.s,a->s.s)!=0);}
O gtd(O a,O b){R newod(a->d>b->d);}
OTF gtf[]={gtd,gts};

V gnop(ST s,OTF*ft,I e){
    I c;O a,b,x,r;b=pop(s);if(b->t==TA){if(e){O ad,bd;a=pop(s);if(a->t!=TA)TE;ad=newod(len(a->a));bd=newod(len(b->a));psh(s,ft[TD](ad,bd));dlo(ad);dlo(bd);dlo(a);dlo(b);R;}else{psh(s,opa(b,ft,e));dlo(b);R;}}
    a=pop(s);if(a->t==TA){r=newoa(newst(BZ));while(len(a->a)){psh(s,pop(a->a));psh(s,dup(b));gnop(s,ft,e);psh(r->a,pop(s));}dlo(a);dlo(b);rev(r->a);psh(s,r);R;}
    c=a->t==TCB||b->t==TCB;/*two different types added together==str*/if(a->t!=b->t){O ao=a,bo=b;a=tosocb(ao);b=tosocb(bo);dlo(ao);dlo(bo);}r=ft[a->t==TCB?TS:a->t](a,b);if(c&&r->t==TS){x=r;r=newocb(x->s.s,x->s.z);dlo(x);}
    psh(s,r);dlo(a);dlo(b);
} //generic op

O muls(O a,O b){S r,p;I i,t=b->d/*truncate*/;L z=a->s.z*t;p=r=alc(z+1);for(i=0;i<t;++i){memcpy(p,a->s.s,a->s.z);p+=a->s.z;}r[z]=0;R newosk(r,z);} //mul strings
O muld(O a,O b){R newod(a->d*b->d);} //mul decimal
V mul(ST s){O a,b;b=pop(s);if(b->t==TA){while(len(b->a)>1)mul(b->a);psh(s,dup(top(b->a)));dlo(b);R;};a=pop(s);if(a->t==TA)TE;if(a->t==TS){if(b->t!=TD)TE;psh(s,muls(a,b));}else psh(s,muld(a,b));dlo(a);dlo(b);} //mul

O moda(O a,O b){ST r=newst(BZ);L i;for(i=0;i<len(a->a);++i)psh(r,dup(a->a->st[i]));for(i=0;i<len(b->a);++i)psh(r,dup(b->a->st[i]));R newoa(r);} //mod array
O modd(O a,O b){if(b->d==0)ex("zero division");R newod(fmod(a->d,b->d));} //mod decimal
O mods(O a,O b){
    L z;S s;C d[BZ];Reprog*p;Resub rs[10];O r,os=pop(top(rst));if(os->t!=TS)TE;s=os->s.s;p=regcomp(a->s.s);if(!p)ex("bad regex");memset(rs,0,sizeof(rs));
    for(r=newos("",0);s<os->s.s+os->s.z&&regexec(p,s,rs,10);s=rs[0].e.ep,memset(rs,0,sizeof(rs))){if(rs[0].s.sp>s){z=rs[0].s.sp-s;r->s.s=rlc(r->s.s,r->s.z+z);memcpy(r->s.s+r->s.z,s,z);r->s.z+=z;}if(b->s.z==0)continue;regsub(b->s.s,d,BZ,rs,sizeof(rs));z=strlen(d);r->s.s=rlc(r->s.s,r->s.z+z);memcpy(r->s.s+r->s.z,d,z);r->s.z+=z;}
    if(s<os->s.s+os->s.z){z=os->s.s+os->s.z-s;r->s.s=rlc(r->s.s,r->s.z+z);memcpy(r->s.s+r->s.z,s,z);r->s.z+=z;}r->s.s=rlc(r->s.s,r->s.z+1);r->s.s[r->s.z]=0;dlo(os);DL(p);R r;
}
OTF modfn[]={modd,mods,moda};
V mod(ST s){O a,b=pop(s);a=pop(s);if(a->t!=b->t||a->t==TCB||b->t==TCB)TE;psh(s,modfn[a->t](a,b));dlo(a);dlo(b);} //mod

V divd(O a,O b,ST s){if(b->d==0)ex("zero division");psh(s,newod(a->d/b->d));} //div decimal
V divs(O a,O b,ST s){S p,l=a->s.s;if(b->s.z==0){for(p=a->s.s;p<a->s.s+a->s.z;++p)psh(s,newos(p,1));R;}for(p=strstr(a->s.s,b->s.s);p;p=strstr(p+1,b->s.s)){psh(s,newos(l,p-l));l=p+1;}if(*l)psh(s,newos(l,a->s.z-(l-a->s.s)));}
OTS divfn[]={divd,divs,0,0};
V divf(ST s){OTS f;O b=pop(s),a=pop(s);if(a->t!=b->t)TE;f=divfn[a->t];if(!f)TE;f(a,b,s);dlo(a);dlo(b);} //div

V eq(ST s){O a,b;b=pop(s);a=pop(s);if(a->t==TA||b->t==TA)TE;psh(s,newod(eqo(a,b)));dlo(a);dlo(b);} //equal

V rvx(ST s){S r;L z;O o=pop(s);if(o->t!=TS)TE;r=alc(o->s.z+1);for(z=0;z<o->s.z;++z)r[o->s.z-z-1]=o->s.s[z];dlo(o);psh(s,newosk(r,z));}  //reverse object

V idc(ST s,C c){O o=pop(s);if(o->t!=TD)TE;psh(s,newod(c=='('?o->d-1:o->d+1));dlo(o);} //inc/dec

V opar(){ST r;O a=pop(top(rst));L i;psh(rst,r=newst(BZ));for(i=0;i<len(a->a);++i)psh(r,a->a->st[i]);} //open array

V evn(ST s){O o=pop(s);if(o->t==TD)psh(s,newod((I)o->d%2==0));else if(o->t==TS){psh(s,dup(o));psh(s,newod(o->s.z));}else if(o->t==TA){psh(s,dup(o));psh(s,newod(len(o->a)));}else TE;dlo(o);} //even? or push string length or push array length

O low(O o){S r=alc(o->s.z+1);L i;for(i=0;i<o->s.z;++i)r[i]=tolower(o->s.s[i]);R newosk(r,o->s.z);} //lowercase
O neg(O o){if(o->t==TD)R newod(-o->d);if(o->t!=TS)TE;R low(o);} //negate

V range(ST s){I i;O o=pop(s);if(o->t!=TD)TE;for(i=o->d/*truncate*/;i>-1;--i)psh(s,newod(i));dlo(o);}

O hsho(O);
O hshd(O o){R dup(o);} //hash decimal
O hshs(O o){L z;S e;F r=0;if(o->s.z==0)R newod(0);r=strtod(o->s.s,&e);if(!*e)R newod(r);for(z=0;z<o->s.z-1;++z)r+=(I)o->s.s[z]*pow(31,o->s.z-z-1);r+=o->s.s[o->s.z-1];R newod(r);} //hash string
O hsha(O o){ST a=newst(BZ);L i;for(i=0;i<len(o->a);++i)psh(a,hsho(o->a->st[i]));R newoa(a);} //hash array
OTB hshf[]={hshd,hshs,hsha,0}; //hash functions
O hsho(O o){OTB f=hshf[o->t];if(f==0)TE;R f(o);} //hash any object
V hsh(ST s){O o=pop(s);psh(s,hsho(o));dlo(o);} //hash

S exc(C);V excb(O);V eval(ST st){S s;O o=pop(st);if(o->t==TS){for(s=o->s.s;s<o->s.s+o->s.z;++s)exc(*s);dlo(o);}else if(o->t==TCB){excb(o);dlo(o);}else TE;}

//math
typedef F(*MF)(F); //math function
V math(MF f,ST s){O n=pop(s);if(n->t!=TD)TE;psh(s,newod(f(n->d)));dlo(n);} //generic math op
V mdst(ST s){O ox,oy;F x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=pow(ox->d,2);y=pow(oy->d,2);psh(s,newod(sqrt(x+y)));dlo(ox);dlo(oy);} //math md
V mrng(ST s){O ox,oy;F f,x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=ox->d;y=oy->d;if(y>x)for(f=x;f<=y;++f)psh(s,newod(f));else if(x>y)for(f=x;f>=y;--f)psh(s,newod(f));dlo(ox);dlo(oy);} //math mr range

V po(FP f,O o){S s=tos(o);fputs(s,f);DL(s);} //print object
S put(O o,I n){po(stdout,o);if(n)putchar('\n');dlo(o);R 0;} //print to output

I pcb=0,ps=0,pf=0,pm=0,pc=0,pv=0,pl=0,pe=0,init=1,icb=0,cbi=0; //codeblock?,string?,file?,math?,char?,var?,lambda?,escape sequence?,init?(used to clear var table on first run),in codeblock?,codeblock indent

V excb(O o){S w;I icbb=icb/*icb backup*/;icb=1;for(w=o->s.s;*w;++w)exc(*w);icb=icbb;} //execute code block

static O v[256]; //variables; indexed by char code; undefined vars are null

V fdo(ST s){I d;O on=v['n'];O b=pop(s);O n=pop(s);if(b->t!=TCB||n->t!=TD)TE;for(d=0;d<n->d;++d){v['n']=newod(d);excb(b);dlo(v['n']);}dlo(n);dlo(b);v['n']=on;} //do loop
V fif(ST s){O f=pop(s),t=pop(s),c=pop(s);truth(c)?t->t==TCB?excb(t):psh(s,t):f->t==TCB?excb(f):psh(s,f);dlo(c);dlo(t);dlo(f);} //if stmt
V fwh(ST s){O b=pop(s),c=top(s);if(b->t!=TCB)TE;while(truth(c)){excb(b);c=top(s);}dlo(b);} //while loop

V take(){O o;if(len(rst)<2)ex("take needs open array");psh(top(rst),pop(rst->st[len(rst)-2]/*previous stack*/));} //take

I isnum(S s){while(*s){if(isdigit(*s++)==0)R 1;}R 1;}//is string number? (helper func)
V rdq(ST s,I u){S e,i=rdln();F d=strtod(i,&e);if(*e)psh(s,newoskz(i));else{DL(i);psh(s,newod(d));}if(u)v['Q']=dup(top(s));} //q,Q

C pec(C c){static C em[]="abtnvf";S p;if(p=strchr(em,c))R 0x7+(p-em);else R c;} //parse escape code

V toca(ST st,O o){ST ca=newst(o->s.z+1);I p=0;while(p<o->s.z){C c[2]={o->s.s[p],0};psh(ca,newos(c,1));p++;}psh(st,newoa(ca));} //string to char array

S exc(C c){
    static S psb; //string buffer
    static S pcbb; //codeblock buffer
    ST st=top(rst);O o;I d; //current stack,temp var for various computations,another temp var
    if(init){memset(v,0,sizeof(v));init=0;}
    if(pl&&!ps&&!pcb&&!pc){C b[2]={c,0};pl=0;psh(st,newocb(b,1));}
    else if(v[c]&&(isalpha(c)?1:!icb)&&!pv&&!ps&&!pc&&!pcb){ //if variable && not defining variable && not parsing string/char
        o=v[c];if(o->t==TCB)excb(o); //if variable is code block and not in code block, run codeblock
        else psh(st,dup(o)); //push variable contents
    } //push/run variable if defined
    else if(pcb&&c&&!ps&&!pc){
        if(c=='{')cbi++;else if(c=='}')cbi--; //create indents if new block is made
        if(cbi<=0){pcbb[pcb-1]=0;psh(st,newocbk(pcbb,pcb-1));pcb=0;} //finish block if indent is 0
        else{pcbb=rlc(pcbb,pcb+1);pcbb[pcb-1]=c;++pcb;} //create code block
    }
    else if(pc&&!ps){if(c=='\\'&&!pe)pe=1;else{C b[2]={pe?pec(c):c,0};pc=pe=0;psh(st,newos(b,1));}}
    else if(ps&&c)
        if(c=='\''&&!pe){exc('"');exc('"');}else{ //string restarting
        if(c=='"'&&!pe){psb[ps-1]=0;psh(st,newosk(psb,ps-1));ps=0;}else if(c=='\\'&&!pe)pe=1;else{psb=rlc(psb,ps+1);psb[ps-1]=pe?pec(c):c;++ps;pe=0;}} //string parsing
    else if(pm&&c){ //math
        pm=0;switch(c){
        #define MO(c,f) case c:math(f,st);BK;
        MO('q',sqrt)MO('[',floor)MO(']',ceil)MO('s',sin)MO('S',asin)MO('c',cos)MO('C',acos)MO('t',tan)MO('T',atan)
        #undef MO
        #define MO(c,f) case c:f(st);BK;
        MO('d',mdst)MO('r',mrng)
        #undef MO
        #define MO(c,v) case c:psh(st,newod(v));BK;
        MO('p',M_PI)MO('e',exp(1.0))MO('l',299792458)
        #undef MO
        default:PE;
    }} //math
    else if(pv){pv=0;if(v[c])dlo(v[c]);v[c]=dup(top(st));} //save var
    else if(isdigit(c))psh(st,newod(c-'0')); //digit
    else if((c>='A'&&c<='F')||(c>='W'&&c<='Z'))psh(st,newod(c-'7')); //number
    else switch(c){ //op
    case ';':dlo(pop(st));BK; //pop
    case '.':psh(st,dup(top(st)));BK; //dup
    case '$':take();BK; //take
    case '_':o=pop(st);psh(st,neg(o));dlo(o);BK; //negate
    case 'e':evn(st);BK;
    case 'r':rev(st);BK; //reverse
    case 'o':case 'p':if((psb=put(pop(st),c=='p')))R psb;BK; //print
    #define OP(o,f,e) case o:gnop(st,f,e);BK;
    OP('+',addf,0)OP('-',subf,0)OP('<',ltf,1)OP('>',gtf,1)
    #undef OP
    case '*':mul(st);BK; //mul
    case '/':divf(st);BK; //div
    case '%':mod(st);BK; //mod
    case '=':eq(st);BK; //eq
    case '`':rvx(st);BK; //reverse obj
    case 'm':pm=1;BK; //begin math
    case ':':pv=1;BK; //begin var
    case '\\':swp(st);BK; //swap
    case '@':rot(st);BK; //rotate 3
    case '#':hsh(st);BK; //hash functions
    case ',':range(st);BK; //range
    case 'G':psh(st,newos("abcdefghijklmnopqrstuvwxyz",26));BK; //alphabet
    case 'J':case 'K':v[c]=dup(top(st));BK; //magic vars
    case 'q':case 'Q':rdq(st,c=='Q');BK; //set input to Q
    case 'i':psh(st,newoskz(rdln()));BK; //read line
    case 'j':psh(st,newod(rdlnd()));BK; //read number
    case 'l':psh(st,newod(len(st)));BK; //push length
    case '~':eval(st);BK; //eval
    case 's':toca(st,pop(st));BK; //string to char array
    case 'S':psh(st,newos("",0));BK; //blank string
    case 'T':psh(st,newos(" ",1));BK; //string w/ space
    case 'U':psh(st,newos("\n",1));BK; //string w/ newline
    case '\'':pc=1;BK; //begin char
    case '"':ps=1;psb=alc(1);BK; //begin string
    case '{':pcb=1;pcbb=alc(1);cbi++;BK; //being codeblock
    case '[':psh(rst,newst(BZ));BK; //begin array
    case ']':if(len(rst)==1)ex("no array to close");pop(rst);psh(top(rst),newoa(st));BK; //end array
    case '(':if(((O)top(st))->t==TA){opar();BK;};case ')':idc(st,c);BK;
    case 'H':case 'I':case 'M':exc('[');exc(c=='H'?'Q':'i');if(c=='M')exc('~');BK; //macros
    case 'L':pl=1;BK; //lambda
    case 'N':exc('{');exc('}');BK; //N macro
    //control flow
    case 'd':fdo(st);BK; //do loop
    case '?':fif(st);BK; //if stmt
    case 'w':fwh(st);BK; //while loop
    case 0://finish
        if(pcb&&!isrepl)exc('}');
        if(ps&&!isrepl)exc('"');
        if((pf||pm||pc||pv)&&!isrepl)ex("unexpected eof");
        if(len(rst)!=1&&!isrepl)ex("eof in array");
        if(len(st))for(d=0;d<len(st);d++)po(stdout,st->st[d]); //print stack to stdout
        #ifdef IDE
        if(d=len(st))fputc('[',SF);while(len(st)){po(SF,top(st));if(len(st)>1)fputc(',',SF);dlo(pop(st));}if(d)fputs("]\n",SF); //print stack to SF w/ formatting
        #else
        while(len(st))dlo(pop(st)); //free stack contents
        #endif
        dls(st);dls(rst);for(d=0;d<sizeof(v)/sizeof(O);++d)if(v[d])dlo(v[d]);init=1;BK; //delete everything
    default:
        if(isalpha(c)&&!v[c])BK; //if undefined variable, just continue
        else PE; //parse error
    }R 0;
} //exec

V excs(S s,I cl){
    if(!rst){rst=newst(BZ);psh(rst,newst(BZ));}ln=1;col=1; //init
    while(*s){while(!ps&&!pc&&isspace(*s)){if(*s=='\n'){++ln;col=0;}else++col;++s;}if(!*s)BK;exc(*s++);++col;} //run
    if(cl){exc(0);rst=0;} //finish
} //exec string

#ifndef UTEST
V repl(){ //repl
    C b[BZ];isrepl=1;printf("O REPL");for(;;){
        printf("\n>>> ");if(!fgets(b,BZ,stdin))BK; //get line
        if(!setjmp(jb))excs(b,0); //run line
    }excs("",1); //cleanup
}

V file(S f){S b;L z;FP fp=fopen(f,"r");if(!fp)ex("file");fseek(fp,0,SEEK_END);z=ftell(fp);fseek(fp,0,SEEK_SET);b=alc(z+1);fread(b,BZ,1,fp);b[z]=0;if(!feof(fp))ex("file error");excs(b,1);DL(b);} //run file

I main(I ac,S*av){if(ac==1)repl();else if(ac==2)file(av[1]);else if(ac==3&&strcmp(av[1],"-e")==0)excs(av[2],1);else ex("arguments");R 0;}

#else //unit tests

#define T(n) V t_##n()
#define TI F vx,vy;O ox,oy;S sx,sy;
#define TF(m,...) do{printf("\nfailure:%d:message:"m"\n",__LINE__,__VA_ARGS__,NULL);++r;}while(0)
#define TEQD(x,y) if((vx=(x))!=(vy=(y)))TF("%f!=%f",vx,vy)
#define TEQI(x,y) TEQD((I)x,(I)y)
#define TEQO(x,y) if(!eqo(ox=(x),oy=(y))){sx=tos(ox);sy=tos(oy);TF("%s!=%s",sx,sy);}dlo(ox);dlo(oy);
#define TEQOD(x,y) TEQO((x),newod(y));
#define TEQOS(x,y) TEQO((x),newosz(y));

I r=0; //how many tests have failed? (doubles as return value)

#define TP pop(top(rst))
#define EX(s) excs(s,0)
#define CL excs("",1)

#define TX(s,t,v) EX(s);TEQO##t(TP,v);CL;

T(stack){TI
    ST s=newst(BZ);psh(s,(P)1);
    TEQI(top(s),1);
    TEQI(len(s),1);
    psh(s,(P)2);TEQI(top(s),2);
    TEQI(len(s),2);
    TEQI(pop(s),2);
    TEQI(top(s),1);
    TEQI(len(s),1);
    psh(s,(P)2);rev(s);TEQI(top(s),1);
    dls(s);
    TX("12l",D,2)
    TX("123l",D,3)
    TX("l",D,0)
}

T(iop){TI //test int ops
    TX("A",D,10)
    TX("B",D,11)
    TX("C",D,12)
    TX("D",D,13)
    TX("E",D,14)
    TX("F",D,15)
    TX("W",D,32)
    TX("X",D,33)
    TX("Y",D,34)
    TX("Z",D,35)
    TX("11+",D,2)
    TX("11-",D,0)
    TX("22*",D,4)
    TX("22/",D,1)
    TX("52/",D,2.5)
    TX("22%",D,0)
    TX("53%",D,2)
    TX("11=",D,1)
    TX("10=",D,0)
    TX("Z",D,35)
    TX("[23]+",D,5)
    TX("[23]*",D,6)
    TX("1(",D,0)
    TX("1)",D,2)
    TX("1e",D,0)
    TX("2e",D,1)
    TX("2_",D,-2)
    TX("2__",D,2)
    TX("4,",D,0)
    TX("4,;",D,1)
    TX("4,;;",D,2)
    TX("4,;;;",D,3)
    TX("4,;;;;",D,4)
    TX("12<",D,1)
    TX("21<",D,0)
    TX("12>",D,0)
    TX("21>",D,1)
}

T(sop){TI //test strings
    TX("\"Hello, world!\"",S,"Hello, world!")
    TX("' ",S," ")
    TX("'\\n",S,"\n")
    TX("'\\v",S,"\v")
    TX("'\\a",S,"\a")
    TX("'\\b",S,"\b")
    TX("'\\f",S,"\f")
    TX("''",S,"'")
    TX("'\\'",S,"'")
    TX("'\\\"",S,"\"")
    TX("\"ab\\tc\\nd\"",S,"ab\tc\nd")
    TX("\"\\\"\"",S,"\"")
    TX("S",S,"")
    TX("T",S," ")
    TX("U",S,"\n")
    TX("G\"abc\"+",S,"abcdefghijklmnopqrstuvwxyzabc")
    TX("\"abc\"G+",S,"abcabcdefghijklmnopqrstuvwxyz")
    TX("\"\"\"\"+",S,"")
    TX("G\"bcd\"-",S,"aefghijklmnopqrstuvwxyz")
    TX("\"s\"1*",S,"s")
    TX("\"s\"0*",S,"")
    TX("\"abcdbe\"\'b/",S,"e")
    TX("\"abcdbe\"\'b/;",S,"cd")
    TX("\"abcdbe\"\'b/;;",S,"a")
    TX("\"abcdb\"\'b/",S,"cd")
    TX("\"abcdb\"\'b/;",S,"a")
    TX("\"abc\"\"\"/",S,"c")
    TX("\"abc\"\"\"/;",S,"b")
    TX("\"abc\"\"\"/;;",S,"a")
    TX("\"abcbd\"'b'c%",S,"acccd")
    TX("\"abcbd\"'b\"c\\\\0\"%",S,"acbccbd")
    TX("\"abcbd\"\"b\"S%",S,"acd")
    TX("GG=",D,1)
    TX("\"\"\"\"=",D,1)
    TX("\"\"G=",D,0)
    TX("[\"ab\"\"cd\"]+",S,"abcd")
    TX("G`",S,"zyxwvutsrqponmlkjihgfedcba")
    TX("Ge",D,26)
    TX("\"ABC\"_",S,"abc")
    TX("\"abc\"_",S,"abc")
    TX("\"\"_",S,"")
    TX("\"12\"~",D,2)
    TX("\"12\"~;",D,1)
    TX("'a",S,"a")
    TX("\"a\"'b",S,"b")
    TX("\"a\"'b;",S,"a")
    TX("'1#",D,1)
    TX("\"1.23\"#",D,1.23)
    TX("'a#",D,97)
    TX("\"ab\"#",D,3105)
    TX("\"acb\"#",D,96384)
    TX("\"abc\"\"ab\"<",D,1)
    TX("\"ab\"\"abc\"<",D,0)
    TX("\"abc\"\"ab\">",D,0)
    TX("\"ab\"\"abc\">",D,1)
}

T(aop){TI //test array ops
    TX("[12](3]+",D,6)
    TX("1[$..]+",D,3)
    TX("[1234]e",D,4)
    TX("[123][1234]>",D,0)
    TX("[123][1234]<",D,1)
    TX("[1234][123]>",D,1)
    TX("[1234][123]<",D,0)
    TX("[1234]1++",D,14)
}

T(vars){TI //test vars
    TX("2a",D,2)
    TX("1:a;a",D,1)
    TX("1:a;a",D,1)
    TX("1:a;2:a;a",D,2)
    TX("2:a1a",D,2)
    TX("1K;K",D,1)
    TX("1KKl",D,2)
    TX("1K",D,1)
    TX("1J;J",D,1)
    TX("1JJl",D,2)
    TX("1J",D,1)
    TX("1:a;'a",S,"a")
    TX("1:a;\"a\"",S,"a")
}

T(codeblocks){TI //test codeblocks
    TX("{2}:a;a",D,2)
    TX("{1:a;a}:c;c",D,1)
    TX("{1}~",D,1)
    TX("\"{1}\"~~",D,1)
    TX("{5:V;V}::;:",D,5)
    TX("{{{1}K;K}J;J}:V;V",D,1)
    TX("1NK;K",D,1)
    TX("L_K;1K",D,-1)
    TX("{1}{2+}+K;K",D,3)
    TX("L1{2+}+K;K",D,3)
    TX("1{2-}+K;K",D,-1)
    TX("{2}1+L-+K;K",D,1)
    TX("1La<",D,0)
    TX("1L1<",D,1)
    TX("1La>",D,0)
    TX("1L1>",D,1)
    TX("1:V{V5+}K;K",D,6)
}

T(flow){TI //test flow control
    TX("25{)}d",D,7)
    TX("5{n}d",D,4)
    TX("5{n}d;",D,3)
    TX("5{n}d;;",D,2)
    TX("5{n}d;;;",D,1)
    TX("5{n}d;;;;",D,0)
    TX("2:n5{}dn",D,2)
    TX("1{5}{6}?",D,5)
    TX("0{5}{6}?",D,6)
    TX("[1]{5}{6}?",D,5)
    TX("[]{5}{6}?",D,6)
    TX("{1}{5}{6}?",D,5)
    TX("{}{5}{6}?",D,6)
    TX("\"abc\"{5}{6}?",D,5)
    TX("'a{5}{6}?",D,5)
    TX("\"\"{5}{6}?",D,6)
    TX("25{(\\)\\}w",D,0)
    TX("25{(\\)\\}w;",D,7)
}

I main(){t_stack();t_iop();t_sop();t_vars();t_codeblocks();t_flow();putchar('\n');R r;}
#endif
