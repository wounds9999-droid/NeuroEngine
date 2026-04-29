#include <jni.h>
#include "ncnn/net.h"
extern "C" JNIEXPORT jstring JNICALL Java_com_neuro_engine_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("C++ Core Active");
}
