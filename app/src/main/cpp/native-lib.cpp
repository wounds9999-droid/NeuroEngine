#include <jni.h>
#include <android/log.h>
#include "ncnn/net.h"
#include "ncnn/gpu.h"

#define LOG_TAG "NeuroEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static ncnn::Net yolov4;

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_NativeCore_initVulkan(JNIEnv* env, jobject thiz, jstring paramPath, jstring binPath) {
    ncnn::create_gpu_instance();
    yolov4.opt.use_vulkan_compute = true;
    yolov4.opt.num_threads = 4;
    
    const char* p = env->GetStringUTFChars(paramPath, 0);
    const char* b = env->GetStringUTFChars(binPath, 0);
    
    yolov4.load_param(p);
    yolov4.load_model(b);
    
    env->ReleaseStringUTFChars(paramPath, p);
    env->ReleaseStringUTFChars(binPath, b);
    LOGI("Vulkan Core Active.");
}
