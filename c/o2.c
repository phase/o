#include <stdlib.h>
#include <string.h>
#include <setjmp.h>
#include <stdio.h>
#include <ctype.h>
#include <math.h>
#ifdef WI
#include <jni.h>
#endif

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
#ifdef WI
typedef jobject JO;
#endif

I ln,col; //line,col
I je=0;jmp_buf jb; //jump on error?,jump buffer

V em(S s){fprintf(stderr,"\nError: line %d, char %d: %s",ln,col,s);} //error message
V ex(S s){em(s);if(je)longjmp(jb,1);else exit(EXIT_FAILURE);} //error and exit
#define TE ex("wrong type") //type error
#define PE em("can't parse") //parse error
P alc(L z){P r;if(!(r=malloc(z)))ex("memory");R r;} //allocate memory
P rlc(P p,L z){P r;if(!(r=realloc(p,z)))ex("memory");R r;} //realloc memory
#define DL(x) free(x)

//stack
typedef struct{P*st;L p,l;}STB;typedef STB*ST; //type:stack,top,len
ST newst(L z){ST s=alc(sizeof(STB));s->st=alc(z*sizeof(P));s->p=0;s->l=z;R s;} //new
V psh(ST s,P x){if(s->p+1==s->l)ex("overflow");s->st[s->p++]=x;} //push
P pop(ST s){if(s->p==0)ex("underflow");R s->st[--s->p];} //pop
P top(ST s){R s->st[s->p-1];} //top
V swp(ST s){P a,b;a=pop(s);b=pop(s);psh(s,a);psh(s,b);} //swap
V rot(ST s){P a,b,c;a=pop(s);b=pop(s);c=pop(s);psh(s,b);psh(s,a);psh(s,c);} //rotate 3
L len(ST s){R s->p;}
V dls(ST s){DL(s->st);DL(s);} //delete
V rev(ST s){P t;L i;for(i=0;i<s->p/2;++i){t=s->st[i];s->st[i]=s->st[s->p-i-1];s->st[s->p-i-1]=t;}} //reverse

ST rst=0; //root stack

//objects
typedef enum{TD,TS,TA}OT; //decimal,string,array
typedef struct{OT t;union{F d;struct{S s;L z;}s;ST a;};}OB;typedef OB*O; //type:type flag,value{decimal,{string,len},array}
S tos(O o){
    S r;switch(o->t){
    case TD:r=alc(BZ)/*hope this is big enough!*/;if(o->d==(I)o->d)sprintf(r,"%d",(I)o->d);else sprintf(r,"%f",o->d);BK;
    case TS:r=alc(o->s.z+1);memcpy(r,o->s.s,o->s.z);r[o->s.z]=0;BK;
    case TA:TE;BK;
    }R r;
} //tostring (copies)
O newo(){R alc(sizeof(OB));} //new object
O newod(F d){O r=newo();r->t=TD;r->d=d;R r;} //new object decimal
O newos(S s,L z){O r=newo();r->t=TS;r->s.s=alc(z+1);memcpy(r->s.s,s,z);r->s.s[z]=0;r->s.z=z;R r;} //new object string (copies)
O newosk(S s,L z){O r=newo();r->t=TS;r->s.s=s;r->s.z=z;R r;} //new object string (doesn't copy)
O newosz(S s){R newos(s,strlen(s));} //new object string w/o len (copies)
O newoa(ST a){O r=newo();r->t=TA;r->a=a;R r;} //new object array
V dlo(O o){
    switch(o->t){
    case TS:DL(o->s.s);BK;
    case TA:while(len(o->a))dlo(pop(o->a));dls(o->a);BK;
    case TD:BK;
    }DL(o);
} //delete object
O toso(O o){S s=tos(o);O r=newosz(s);DL(s);R r;} //wrap tostring in object
O dup(O o){
    S s;switch(o->t){
    case TS:R newos(o->s.s,o->s.z);BK;
    case TD:R newod(o->d);BK;
    case TA:TE;BK; //XXX:shouldn't be a type error
    }R 0; //appease the compiler
} //dup
I eqo(O a,O b){
    if(a->t!=b->t)R 0;
    switch(a->t){
    case TS:R a->s.z!=b->s.z?0:memcmp(a->s.s,b->s.s,a->s.z)==0;
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

V divs(O a,O b,ST s){L i,p=0;for(i=0;i<a->s.z-b->s.z;++i)if(memcmp(a->s.s+i,b->s.s,b->s.z)==0){psh(s,newos(a->s.s+p,i-p));p=i;}if(i<a->s.z)psh(s,newos(a->s.s+p,i-p));}

V eq(ST s){O a,b;b=pop(s);a=pop(s);if(a->t==TA||b->t==TA)TE;psh(s,newod(eqo(a,b)));dlo(a);dlo(b);} //equal

V revx(ST s){S r;L z;O o=pop(s);if(o->t!=TS)TE;r=alc(o->s.z+1);for(z=0;z<o->s.z;++z)r[o->s.z-z-1]=o->s.s[z];dlo(o);psh(s,newos(r,z));}

//math
typedef F(*MF)(F); //math function
V math(MF f,ST s){O n=pop(s);if(n->t!=TD)TE;psh(s,newod(f(n->d)));dlo(n);} //generic math op
V mdst(ST s){O ox,oy;F x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=pow(ox->d,2);y=pow(oy->d,2);psh(s,newod(sqrt(x+y)));dlo(ox);dlo(oy);} //math md
V mrng(ST s){O ox,oy;F f,x,y;oy=pop(s);ox=pop(s);if(ox->t!=TD||oy->t!=TD)TE;x=ox->d;y=oy->d;if(y>x)for(f=x;f<=y;++f)psh(s,newod(f));else if(x>y)for(f=x;f>=y;--f)psh(s,newod(f));dlo(ox);dlo(oy);} //math mr range

V po(FP f,O o){I i;if(o->t==TA){fprintf(f,"[");for(i=0;i<len(o->a);++i){if(i)fprintf(f,",");po(f,o->a->st[i]);}fprintf(f,"]");}else{S s=tos(o);fputs(s,f);DL(s);}} //print object
#ifdef WI
S put(O o,I n){S s=tos(o);Z l=strlen(s);if(n){s=rlc(s,l+2);s[l]='\n';s[l+1]=0;}R s;}
#else
S put(O o,I n){po(stdout,o);if(n)putchar('\n');dlo(o);R 0;} //print to output
#endif

S exc(C c,ST sts){
    static S psb; //string buffer
    static I pcb=0,ps=0,pf=0,pm=0,pc=0,pv=0,psl; //codeblock?,string?,file?,math?,char?,var?
    ST st=top(sts);O o;I d=len(st);
    if(ps)if(c=='\"'){psb[ps-1]=0;psh(st,newosk(psb,ps-1));ps=0;}else{psb=rlc(psb,ps+1);psb[ps-1]=c;++ps;} //string
    else if(pm){ //math
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
    else if(isdigit(c))psh(st,newod(c-'0')); //digit
    else if(c>='A'&&c<='Z'&&c!='G')psh(st,newod(c-'7')); //number
    else switch(c){ //op
    case ';':dlo(pop(st));BK; //pop
    case '.':psh(st,dup(top(st)));BK; //dup
    case 'r':rev(st);BK;
    case 'o':case 'p':if(psb=put(pop(st),c=='p'))R psb;BK; //print
    #define OP(o,f) case o:gnop(st,f);BK;
    OP('+',addf)OP('-',subf)
    #undef OP
    case '*':mul(st);BK; //mul
    case '=':eq(st);BK; //eq
    case '`':revx(st);BK;
    case 'm':pm=1;BK; //begin math
    case '\\':swp(st);BK; //swap
    case '@':rot(st);BK; //rotate 3
    case 'G':psh(st,newos("abcdefghijklmnopqrstuvwxyz",26));BK; //alphabet
    case '\"':ps=1;psb=alc(1);BK; //begin string
    case '[':psh(rst,newst(BZ));BK; //begin array
    case ']':pop(rst);psh(top(rst),newoa(st));BK; //end array
    case 0://finish
        if(pcb||ps||pf||pm||pc||pv)ex("unexpected eof");
        if(len(sts)!=1)ex("eof in array");
        if(d)putchar('[');while(len(st)){po(stdout,top(st));if(len(st)>1)putchar(',');dlo(pop(st));}if(d)puts("]");dls(st);dls(sts);BK;
    default:PE;
    }++col;R 0;
} //exec

V excs(S s,I cl){
    if(!rst){rst=newst(BZ);psh(rst,newst(BZ));}ln=1;col=1; //init
    while(*s){while(isspace(*s)){if(*s=='\n'){++ln;col=0;}else++col;++s;}if(!*s)BK;exc(*s++,rst);} //run
    if(cl){exc(0,rst);rst=0;} //finish
} //exec string

#ifdef WI
#include <jni.h>
JNIEXPORT JO JNICALL Java_xyz_jadonfowler_o_OC_parse(JNIEnv*,JO t,JO c){
    excs();
}
#endif

#ifndef UTEST
V repl(){ //repl
    C b[BZ];je=1;printf("O repl");for(;;){
        printf("\n>>> ");if(!fgets(b,BZ,stdin))BK; //get line
        if(!setjmp(jb))excs(b,0); //run line
    }excs("",1); //cleanup
}

V file(S f){C b[BZ];FP fp=fopen(f,"r");if(!fp)ex("file");fread(b,BZ,1,fp);if(!feof(fp))ex("buffer overflow");excs(b,1);} //run file

I main(I ac,S*av){if(ac==1)repl();else if(ac==2)file(av[1]);else ex("arguments");R 0;}
#else //unit tests
#define T(n) V t_##n()
#define TI F vx,vy;O ox,oy;S sx,sy;
#define TF(m,...) printf("failure:%d:message:"m"\n",__LINE__,__VA_ARGS__,NULL);
#define TEQD(x,y) if((vx=(x))!=(vy=(y)))TF("%f!=%f",vx,vy)
#define TEQI(x,y) TEQD((I)x,(I)y)
#define TEQO(x,y) if(!eqo(ox=(x),oy=(y))){sx=tos(ox);sy=tos(oy);TF("%s!=%s",sx,sy);}dlo(ox);dlo(oy);
#define TEQOD(x,y) TEQO((x),newod(y));
#define TEQOS(x,y) TEQO((x),newosz(y));

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
}

#define TP pop(top(rst))
#define EX(s) excs(s,0)
#define CL excs("",1)

#define TX(s,t,v) EX(s);TEQO##t(TP,v);CL;

T(iop){TI //test int ops
    TX("11+",D,2)
    TX("11-",D,0)
    TX("22*",D,4)
    TX("11=",D,1)
    TX("10=",D,0)
    TX("Z",D,35)
    TX("[23]+",D,5)
    TX("[23]*",D,6)
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
}

I main(){t_stack();t_iop();t_sop();R 0;}
#endif
