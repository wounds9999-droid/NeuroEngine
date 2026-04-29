#include <jni.h>
#include <string>
#include "ncnn/net.h"
extern "C" JNIEXPORT jstring JNICALL Java_com_neuro_engine_NativeCore_initEngine(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("Engine Init");
}
