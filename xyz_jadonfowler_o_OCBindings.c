#ifdef WI
#include "xyz_jadonfowler_o_OCBindings.h"
#include "o2.c"
#include <jni.h>

typedef jstring JS;
typedef jobject JO;
typedef jobjectArray JOA;
typedef jclass JC;
typedef jmethodID JMID;

JNIEXPORT JS JNICALL Java_xyz_jadonfowler_o_OCBindings_parse(JNIEnv*e,JO t,JO oc){S s;JO r;JMID mc;if(!rst)excs("",0);mc=(*e)->GetMethodID(e,(*e)->FindClass(e,"java/lang/Character"),"charValue","()C");if(!mc)R 0;s=exc((*e)->CallCharMethod(e,oc,mc),rst);r=(*e)->NewStringUTF(e,s);DL(s);R r;}

JNIEXPORT V JNICALL Java_xyz_jadonfowler_o_OCBindings_cl(JNIEnv*e,JO t){excs("",1);}

JNIEXPORT V JNICALL Java_xyz_jadonfowler_o_OCBindings_setInputs(JNIEnv*e,JO t,JOA s){/*TODO: Set local inputs"*/}

JNIEXPORT V JNICALL Java_xyz_jadonfowler_o_OCBindings_setInputPointer(JNIEnv*e,JO t,JO ptr){/*TODO: Set input pointer*/}

JNIEXPORT JS JNICALL Java_xyz_jadonfowler_o_OCBindings_getCurrentStackContents(JNIEnv*e,JO t){R (*e)->NewStringUTF(e, "stack contents");/*TODO: Return current stack contents in a nice string*/}

#endif