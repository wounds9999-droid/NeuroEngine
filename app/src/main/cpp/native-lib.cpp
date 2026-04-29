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
    // يتم تحميل الأوزان هنا
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_VisionService_processFrame(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (window) {
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
            memset(buffer.bits, 0, buffer.stride * buffer.height * 4);
            
            // رسم مربع رادار كمؤشر للعمل
            uint32_t* line = (uint32_t*)buffer.bits;
            for (int i = 200; i < 300; i++) {
                for(int j = 200; j < 205; j++) {
                    line[j + (buffer.stride * i)] = 0xFF00FF00;
                }
            }
            
            ANativeWindow_unlockAndPost(window);
        }
        ANativeWindow_release(window);
    }
}
