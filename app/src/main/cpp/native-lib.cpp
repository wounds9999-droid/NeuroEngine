#include <jni.h>
#include <android/native_window_jni.h>
#include <string.h>
#include <unistd.h>

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_VisionService_processFrame(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (window) {
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
            memset(buffer.bits, 0, buffer.stride * buffer.height * 4);
            
            // Draw Human-Like Aim/Radar Visuals
            uint32_t* pixels = (uint32_t*)buffer.bits;
            int cx = buffer.width / 2;
            int cy = buffer.height / 2;
            
            // Crosshair Alpha
            for(int i = -20; i < 20; i++) {
                if(cx+i >= 0 && cx+i < buffer.width) pixels[cy * buffer.stride + (cx+i)] = 0xFF00FF00;
                if(cy+i >= 0 && cy+i < buffer.height) pixels[(cy+i) * buffer.stride + cx] = 0xFF00FF00;
            }
            
            ANativeWindow_unlockAndPost(window);
        }
        ANativeWindow_release(window);
    }
}
