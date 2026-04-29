#include <jni.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "net.h"

static ncnn::Net neuroNet;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_neuro_engine_VisionService_loadModel(JNIEnv* env, jobject thiz, jobject assetManager) {
    ncnn::Option opt;
    opt.use_vulkan_compute = true;
    neuroNet.opt = opt;
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_VisionService_processFrame(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (window) {
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
            memset(buffer.bits, 0, buffer.stride * buffer.height * 4); // تنظيف الشاشة
            
            // رسم مربع الرادار الأخضر
            uint32_t* line = (uint32_t*)buffer.bits;
            for (int i = 300; i < 400; i++) {
                for(int j = 300; j < 305; j++) {
                    line[j + (buffer.stride * i)] = 0xFF00FF00;
                }
            }
            ANativeWindow_unlockAndPost(window);
        }
        ANativeWindow_release(window);
    }
}
