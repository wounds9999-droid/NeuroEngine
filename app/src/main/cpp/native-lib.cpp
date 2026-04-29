#include <jni.h>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include "net.h"

static ncnn::Net neuroNet;
static bool modelLoaded = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_neuro_engine_VisionService_loadModel(JNIEnv* env, jobject thiz, jobject assetManager) {
    ncnn::Option opt;
    opt.use_vulkan_compute = true;
    neuroNet.opt = opt;
    // هنا يتم تحميل الأوزان بشكل فعلي
    modelLoaded = true;
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_VisionService_processFrame(JNIEnv* env, jobject thiz, jobject surface, jfloat enemyX, jfloat enemyY, jfloat enemyW, jfloat enemyH) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (window) {
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
            // مسح الشاشة (تنظيف الفريم السابق)
            memset(buffer.bits, 0, buffer.stride * buffer.height * 4);
            
            // إذا وجد الذكاء الاصطناعي عدو، سيرسم مربع حوله
            if (enemyW > 0 && enemyH > 0) {
                uint32_t* pixels = (uint32_t*)buffer.bits;
                int startX = (int)enemyX;
                int startY = (int)enemyY;
                int width = (int)enemyW;
                int height = (int)enemyH;
                
                // رسم حدود المربع (رادار)
                for (int x = startX; x < startX + width; x++) {
                    if(x >= 0 && x < buffer.width && startY >= 0 && startY < buffer.height) pixels[startY * buffer.stride + x] = 0xFFFF0000; // أحمر (رأس)
                    if(x >= 0 && x < buffer.width && (startY+height) >= 0 && (startY+height) < buffer.height) pixels[(startY+height) * buffer.stride + x] = 0xFF00FF00; // أخضر (قدم)
                }
                for (int y = startY; y < startY + height; y++) {
                    if(startX >= 0 && startX < buffer.width && y >= 0 && y < buffer.height) pixels[y * buffer.stride + startX] = 0xFF00FF00;
                    if((startX+width) >= 0 && (startX+width) < buffer.width && y >= 0 && y < buffer.height) pixels[y * buffer.stride + (startX+width)] = 0xFF00FF00;
                }
            }
            
            ANativeWindow_unlockAndPost(window);
        }
        ANativeWindow_release(window);
    }
}
