#include <jni.h>
#include <android/native_window_jni.h>
#include <string.h>
#include <stdint.h>

void drawBox(uint32_t* pixels, int stride, int x, int y, int w, int h, uint32_t color) {
    for (int i = 0; i < w; i++) {
        pixels[y * stride + (x + i)] = color;
        pixels[(y + h) * stride + (x + i)] = color;
    }
    for (int i = 0; i < h; i++) {
        pixels[(y + i) * stride + x] = color;
        pixels[(y + i) * stride + (x + w)] = color;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_neuro_engine_VisionService_nativeRender(JNIEnv* env, jobject thiz, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (!window) return;

    ANativeWindow_Buffer buffer;
    if (ANativeWindow_lock(window, &buffer, NULL) == 0) {
        uint32_t* pixels = (uint32_t*)buffer.bits;
        memset(pixels, 0, buffer.stride * buffer.height * 4);

        int screenW = buffer.width;
        int screenH = buffer.height;

        // Dynamic Radar Simulation (Multiple Targets)
        drawBox(pixels, buffer.stride, (screenW/2)-150, (screenH/2)-250, 300, 500, 0xFF00FF00); // Main Target
        drawBox(pixels, buffer.stride, 100, 200, 100, 200, 0xFFFF0000); // Distant Target 1
        drawBox(pixels, buffer.stride, screenW-200, 400, 120, 240, 0xFFFFFF00); // Distant Target 2

        // Center Crosshair
        int cx = screenW / 2;
        int cy = screenH / 2;
        for(int i = -15; i <= 15; i++) {
            pixels[cy * buffer.stride + (cx + i)] = 0xFF00FF00;
            pixels[(cy + i) * buffer.stride + cx] = 0xFF00FF00;
        }

        ANativeWindow_unlockAndPost(window);
    }
    ANativeWindow_release(window);
}
