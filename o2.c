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

I ln,col; //line,col
I isrepl=0;jmp_buf jb; //repl(implies jump on error)?,jump buffer

V em(S s){fprintf(stderr,"\nError @%d:%d: %s",ln,col,s);} //error message
V ex(S s){em(s);if(isrepl)longjmp(jb,1);else exit(EXIT_FAILURE);} //error and exit
#define TE ex("wrong type") //type error
#define PE ex("can't parse") //parse error
#define PXE ex(strerror(errno))
P alc(L z){P r;if(!(r=malloc(z)))ex("memory");R r;} //allocate memory
P rlc(P p,L z){P r;if(!(r=realloc(p,z)))ex("memory");R r;} //realloc memory
#define DL(x) free(x)

S rdln(){S r=alc(BZ);if(!fgets(r,BZ,stdin))PXE;r[strlen(r)-1]=0;R r;} //read line(XXX:only allows BZ as max length!)
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
typedef struct{OT t;union{F d;struct{S s;L z;}s;ST a;};}OB;typedef OB*O; //type:type flag,value{decimal,{string,len},array}
S tos(O o){
    S r;switch(o->t){
    case TD:r=alc(BZ)/*hope this is big enough!*/;if(o->d==(I)o->d)sprintf(r,"%d",(I)o->d);else sprintf(r,"%f",o->d);BK;
    case TS:case TCB:r=alc(o->s.z+1);memcpy(r,o->s.s,o->s.z);r[o->s.z]=0;BK;
    case TA:strcat(r,"[");I l=len(o->a);if(l){I i;for(i=0;i<l;++i){
        if(i) strcat(r,",");strcat(r,tos(o->a->st[i]));
    }}strcat(r,"]");BK;
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
O dup(O o){
    S s;switch(o->t){
    case TCB:R newocb(o->s.s,o->s.z);BK;
    case TS:R newos(o->s.s,o->s.z);BK;
    case TD:R newod(o->d);BK;
    case TA:TE;BK; //XXX:shouldn't be a type error
    }R 0; //appease the compiler
} //dup
I eqo(O a,O b){
    if(a->t!=b->t)R 0;
    switch(a->t){
    case TS:case TCB:R a->s.z!=b->s.z?0:memcmp(a->s.s,b->s.s,a->s.z)==0;
    case TD:R a->d==b->d;
    default:ex("non-TS-TD in eqo");R 0;
    }
} //equal

//stack-object manips(obj args are freed by caller)
typedef O(*OTF)(O,O); //function spec type (e.g. adds, addd, etc.)
V gnop(ST,OTF*);
O opa(O o,OTF*ft){while(len(o->a)>1)gnop(o->a,ft);R dup(top(o->a));} //apply op to array elements

O adds(O a,O b){S rs=alc(a->s.z+b->s.z+1);memcpy(rs,a->s.s,a->s.z);memcpy(rs+a->s.z,b->s.s,b->s.z+1);R newosk(rs,a->s.z+b->s.z);} //add strings
O addd(O a,O b){R newod(a->d+b->d);} //add decimal
OTF addf[]={addd,adds};

O subs(O a,O b){L i,z=a->s.z;S r,p;if(b->s.z==0)R dup(a);for(i=0;i<a->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0)z-=b->s.z;p=r=alc(z+1);for(i=0;i<a->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0)i+=b->s.z-1;else*p++=a->s.s[i];R newosk(r,z);} //sub strings
O subd(O a,O b){R newod(a->d-b->d);} //sub decimal
OTF subf[]={subd,subs};

V gnop(ST s,OTF*ft){O a,b;b=pop(s);if(b->t==TA){psh(s,opa(b,ft));dlo(b);R;};a=pop(s);if(a->t==TA)TE;/*str+any or any+str==str+str*/if(a->t==TS&&b->t!=TS){O bo=b;b=toso(b);dlo(bo);}else if(b->t==TS&&a->t!=TS){O ao=a;a=toso(a);dlo(ao);}psh(s,ft[a->t](a,b));dlo(a);dlo(b);} //generic op

O muls(O a,O b){S r,p;I i,t=b->d/*truncate*/;L z=a->s.z*t;p=r=alc(z+1);for(i=0;i<t;++i){memcpy(p,a->s.s,a->s.z);p+=a->s.z;}r[z]=0;R newosk(r,z);} //mul strings
O muld(O a,O b){R newod(a->d*b->d);} //mul decimal
V mul(ST s){O a,b;b=pop(s);if(b->t==TA){while(len(b->a)>1)mul(b->a);psh(s,dup(top(b->a)));dlo(b);R;};a=pop(s);if(a->t==TA)TE;if(a->t==TS){if(b->t!=TD)TE;psh(s,muls(a,b));}else psh(s,muld(a,b));dlo(a);dlo(b);} //mul

O moda(O a,O b){ST r=newst(BZ);L i;for(i=0;i<len(a->a);++i)psh(r,dup(a->a->st[i]));for(i=0;i<len(b->a);++i)psh(r,dup(b->a->st[i]));R newoa(r);} //mod array
O modd(O a,O b){R newod(fmod(a->d,b->d));} //mod decimal
O mods(O a,O b){TE;R a;} //TODO: mod string
OTF modfn[]={modd,mods,moda};
V mod(ST s){O a,b=pop(s);a=pop(s);if(a->t!=b->t)TE;psh(s,modfn[a->t](a,b));dlo(a);dlo(b);} //mod

V divs(O a,O b,ST s){L i,p=0;for(i=0;i<a->s.z-b->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0){psh(s,newos(a->s.s+p,i-p));p=i;}if(i<a->s.z)psh(s,newos(a->s.s+p,i-p));dlo(a);dlo(b);}

V eq(ST s){O a,b;b=pop(s);a=pop(s);if(a->t==TA||b->t==TA)TE;psh(s,newod(eqo(a,b)));dlo(a);dlo(b);} //equal

V rvx(ST s){S r;L z;O o=pop(s);if(o->t!=TS)TE;r=alc(o->s.z+1);for(z=0;z<o->s.z;++z)r[o->s.z-z-1]=o->s.s[z];dlo(o);psh(s,newosk(r,z));}  //reverse object

V idc(ST s,C c){O o=pop(s);if(o->t!=TD)TE;psh(s,newod(c=='('?o->d-1:o->d+1));dlo(o);} //inc/dec

V opar(ST rst){ST r;O a=pop(top(rst));L i;psh(rst,r=newst(BZ));for(i=0;i<len(a->a);++i)psh(r,a->a->st[i]);} //open array

V evn(ST s){O o=pop(s);if(o->t==TD)psh(s,newod((I)o->d%2==0));else if(o->t==TS)psh(s,newod(o->s.z));else TE;dlo(o);} //even?

O low(O o){S r=alc(o->s.z+1);L i;for(i=0;i<o->s.z;++i)r[i]=tolower(o->s.s[i]);R newosk(r,o->s.z);} //lowercase
O neg(O o){if(o->t==TD)R newod(-o->d);if(o->t!=TS)TE;R low(o);} //negate

V range(ST s){I i;O o=pop(s);if(o->t!=TD)TE;for(i=o->d/*truncate*/;i>-1;--i)psh(s,newod(i));dlo(o);}

O hshs(O o){L z;S e;F r=0;if(o->s.z==0)R newod(0);r=strtod(o->s.s,&e);if(!*e)R newod(r);for(z=0;z<o->s.z-1;++z)r+=(I)o->s.s[z]*pow(31,o->s.z-z-1);r+=o->s.s[o->s.z-1];R newod(r);}
V hsh(ST s){/*XXX:Java O also hashes arrays*/O o=pop(s);if(o->t==TD){psh(s,o);dlo(o);R;}if(o->t!=TS)TE;psh(s,hshs(o));dlo(o);} //hash

S exc(C,ST);V eval(ST sts){S s;O o=pop(top(sts));if(o->t!=TS)TE;for(s=o->s.s;s<o->s.s+o->s.z;++s)exc(*s,sts);dlo(o);}

//math
typedef F(*MF)(F); //math function
V math(MF f,ST s){O n=pop(s);if(n->t!=TD)TE;psh(s,newod(f(n->d)));dlo(n);} //generic math op
V mdst(ST s){O ox,oy;F x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=pow(ox->d,2);y=pow(oy->d,2);psh(s,newod(sqrt(x+y)));dlo(ox);dlo(oy);} //math md
V mrng(ST s){O ox,oy;F f,x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=ox->d;y=oy->d;if(y>x)for(f=x;f<=y;++f)psh(s,newod(f));else if(x>y)for(f=x;f>=y;--f)psh(s,newod(f));dlo(ox);dlo(oy);} //math mr range

V po(FP f,O o){S s=tos(o);fputs(s,f);DL(s);} //print object
#ifdef WI
S put(O o,I n){S s=tos(o);L l=strlen(s);if(n){s=rlc(s,l+2);s[l]='\n';s[l+1]=0;}R s;}
#else
S put(O o,I n){po(stdout,o);if(n)putchar('\n');dlo(o);R 0;} //print to output
#endif

S exc(C c,ST sts){
    static S psb; //string buffer
    static S pcbb; //codeblock buffer
    static I pcb=0,ps=0,pf=0,pm=0,pc=0,pv=0,init=1,icb=0; //codeblock?,string?,file?,math?,char?,var?,init?(used to clear v on first run), in codeblock?
    ST st=top(sts);O o;I d=len(st);
    static O v[256];if(init){memset(v,0,sizeof(v));init=0;} //variables; indexed by char code; undefined vars are null
    if(v[c]&&!icb&&!pv){ //if variable && not in codeblock && no defining variable
        o=v[c];if(o->t==TCB){ //if variable is codeblock
            S w;icb=1;for(w=o->s.s;*w;++w)exc(*w,sts);icb=0;} //run codeblock
        else psh(st,dup(o)); //push variable contents
    } //push/run variable if defined
    else if(pcb&&c)if(c=='}'){pcbb[pcb-1]=0;psh(st,newocbk(pcbb,pcb-1));pcb=0;}else{pcbb=rlc(pcbb,pcb+1);pcbb[pcb-1]=c;++pcb;} //code block
    else if(pc){C b[2]={c,0};pc=0;psh(st,newos(b,1));}
    else if(ps&&c)if(c=='"'||c=='\''){psb[ps-1]=0;psh(st,newosk(psb,ps-1));ps=0;if(c=='\'')pc=1;}else{psb=rlc(psb,ps+1);psb[ps-1]=c;++ps;} //string
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
    else if(pv){pv=0;if(!isalpha(c))PE;if(v[c])dlo(v[c]);v[c]=dup(top(st));} //save var
    else if(isdigit(c))psh(st,newod(c-'0')); //digit
    else if((c>='A'&&c<='F')||(c>='W'&&c<='Z'))psh(st,newod(c-'7')); //number
    else switch(c){ //op
    case ';':dlo(pop(st));BK; //pop
    case '.':psh(st,dup(top(st)));BK; //dup
    case '_':o=pop(st);psh(st,neg(o));dlo(o);BK; //negate
    case 'e':evn(st);BK;
    case 'r':rev(st);BK; //reverse
    case 'o':case 'p':if((psb=put(pop(st),c=='p')))R psb;BK; //print
    #define OP(o,f) case o:gnop(st,f);BK;
    OP('+',addf)OP('-',subf)
    #undef OP
    case '*':mul(st);BK; //mul
    case '%':mod(st);BK; //mod
    case '=':eq(st);BK; //eq
    case '`':rvx(st);BK; //reverse obj
    case 'm':pm=1;BK; //begin math
    case ':':pv=1;BK; //begin var
    case '\\':swp(st);BK; //swap
    case '@':rot(st);BK; //rotate 3
    case '#':hsh(st);BK;
    case ',':range(st);BK; //range
    case 'G':psh(st,newos("abcdefghijklmnopqrstuvwxyz",26));BK; //alphabet
    case 'i':psh(st,newoskz(rdln()));BK; //read line
    case 'j':psh(st,newod(rdlnd()));BK; //read number
    case 'l':psh(st,newod(len(st)));BK;
    case '~':eval(sts);BK; //eval
    case '\'':pc=1;BK; //begin char
    case '"':ps=1;psb=alc(1);BK; //begin string
    case '{':pcb=1;pcbb=alc(1);BK; //being codeblock
    case '[':psh(rst,newst(BZ));BK; //begin array
    case ']':if(len(rst)==1)ex("no array to close");pop(rst);psh(top(rst),newoa(st));BK; //end array
    case '(':if(((O)top(st))->t==TA){opar(rst);BK;};case ')':idc(st,c);BK;
    //macros
    case 'H':case 'I':case 'M':exc('[',sts);exc(c=='H'?'Q':'i',sts);if(c=='M')exc('~',sts);BK;
    case 0://finish
        if((pcb||ps||pf||pm||pc||pv)&&!isrepl)ex("unexpected eof");
        if(len(sts)!=1&&!isrepl)ex("eof in array");
        if(d)putchar('[');while(len(st)){po(stdout,top(st));if(len(st)>1)putchar(',');dlo(pop(st));}if(d)puts("]");dls(st);dls(sts);for(d=0;d<sizeof(v)/sizeof(O);++d)if(v[d])dlo(v[d]);init=1;BK;
    default:
        if(isalpha(c)&&!v[c])BK; //if undefined variable, just continue
        else PE; //parse error
    }++col;R 0;
} //exec

V excs(S s,I cl){
    if(!rst){rst=newst(BZ);psh(rst,newst(BZ));}ln=1;col=1; //init
    while(*s){while(isspace(*s)){if(*s=='\n'){++ln;col=0;}else++col;++s;}if(!*s)BK;exc(*s++,rst);} //run
    if(cl){exc(0,rst);rst=0;} //finish
} //exec string

#ifndef UTEST
V repl(){ //repl
    C b[BZ];isrepl=1;printf("O REPL");for(;;){
        printf("\n>>> ");if(!fgets(b,BZ,stdin))BK; //get line
        if(!setjmp(jb))excs(b,0); //run line
    }excs("",1); //cleanup
}

V file(S f){C b[BZ];FP fp=fopen(f,"r");if(!fp)ex("file");fread(b,BZ,1,fp);if(!feof(fp))ex("buffer overflow");excs(b,1);} //run file

I main(I ac,S*av){if(ac==1)repl();else if(ac==2)file(av[1]);else ex("arguments");R 0;}
#else //unit tests
#define T(n) V t_##n()
#define TI F vx,vy;O ox,oy;S sx,sy;
#define TF(m,...) do{printf("failure:%d:message:"m"\n",__LINE__,__VA_ARGS__,NULL);++r;}while(0)
#define TEQD(x,y) if((vx=(x))!=(vy=(y)))TF("%f!=%f",vx,vy)
#define TEQI(x,y) TEQD((I)x,(I)y)
#define TEQO(x,y) if(!eqo(ox=(x),oy=(y))){sx=tos(ox);sy=tos(oy);TF("%s!=%s",sx,sy);}dlo(ox);dlo(oy);
#define TEQOD(x,y) TEQO((x),newod(y));
#define TEQOS(x,y) TEQO((x),newosz(y));

I r=0;

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
}

T(sop){TI //test string ops(I really hate the need to escape all the quotes here)
    TX("G\"abc\"+",S,"abcdefghijklmnopqrstuvwxyzabc")
    TX("\"abc\"G+",S,"abcabcdefghijklmnopqrstuvwxyz")
    TX("\"\"\"\"+",S,"")
    TX("G\"bcd\"-",S,"aefghijklmnopqrstuvwxyz")
    TX("\"s\"1*",S,"s")
    TX("\"s\"0*",S,"")
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
}

T(aop){TI //test array ops
    TX("[12](3]+",D,6)
}

T(vars){TI //test vars & codeblocks
    TX("2a",D,2)
    TX("1:a;a",D,1)
    TX("1:a;a",D,1)
    TX("1:a;2:a;a",D,2)
    TX("2:a1a",D,2)
    TX("{2}:a;a",D,2)
}

I main(){t_stack();t_iop();t_sop();t_vars();R r;}
#endif
