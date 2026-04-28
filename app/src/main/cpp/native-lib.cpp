#include <jni.h>
#include <string>
#include "ncnn/net.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_neuro_engine_NativeCore_stringFromJNI(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("NeuroEngine System Re-Generated");
}
