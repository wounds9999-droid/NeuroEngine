#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "ncnn/net.h"

static ncnn::Net* g_neuro_net = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_neuro_engine_NativeCore_initInferenceEngine(JNIEnv* env, jclass clazz, jobject assetManager, jstring modelPath, jboolean useVulkan) {
    g_neuro_net = new ncnn::Net();
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    g_neuro_net->load_param(mgr, (std::string(path) + ".param").c_str());
    g_neuro_net->load_model(mgr, (std::string(path) + ".bin").c_str());
    env->ReleaseStringUTFChars(modelPath, path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_neuro_engine_NativeCore_processFrame(JNIEnv* env, jclass clazz, jobject bitmap) {
    if (!g_neuro_net) return nullptr;
    AndroidBitmapInfo info;
    void* pixels;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    ncnn::Mat in = ncnn::Mat::from_pixels((const unsigned char*)pixels, ncnn::Mat::PIXEL_RGBA2RGB, info.width, info.height);
    AndroidBitmap_unlockPixels(env, bitmap);
    ncnn::Extractor ex = g_neuro_net->create_extractor();
    ex.input("input_blob", in);
    ncnn::Mat out;
    ex.extract("output_blob", out);
    jfloatArray res = env->NewFloatArray(out.w);
    env->SetFloatArrayRegion(res, 0, out.w, out);
    return res;
}
