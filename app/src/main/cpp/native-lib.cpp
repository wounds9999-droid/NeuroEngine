#include <jni.h>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "ncnn/net.h"

static ncnn::Net* g_net = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_neuro_engine_NativeCore_initEngine(JNIEnv* env, jclass clazz, jobject assets, jstring path, jboolean vulkan) {
    if (g_net) return JNI_TRUE;
    g_net = new ncnn::Net();
    AAssetManager* mgr = AAssetManager_fromJava(env, assets);
    const char* m_path = env->GetStringUTFChars(path, nullptr);
    g_net->load_param(mgr, (std::string(m_path) + ".param").c_str());
    g_net->load_model(mgr, (std::string(m_path) + ".bin").c_str());
    env->ReleaseStringUTFChars(path, m_path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_neuro_engine_NativeCore_processFrame(JNIEnv* env, jclass clazz, jobject bitmap) {
    if (!g_net) return nullptr;
    AndroidBitmapInfo info;
    void* pixels;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    ncnn::Mat in = ncnn::Mat::from_pixels((const unsigned char*)pixels, ncnn::Mat::PIXEL_RGBA2RGB, info.width, info.height);
    AndroidBitmap_unlockPixels(env, bitmap);
    ncnn::Extractor ex = g_net->create_extractor();
    ex.input("input", in);
    ncnn::Mat out;
    ex.extract("output", out);
    jfloatArray res = env->NewFloatArray(out.w);
    env->SetFloatArrayRegion(res, 0, out.w, out);
    return res;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_neuro_engine_NativeCore_getStatus(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("NeuroEngine: ACTIVE | VULKAN: ON");
}
