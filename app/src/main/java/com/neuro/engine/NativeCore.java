package com.neuro.engine;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
public class NativeCore {
    static { System.loadLibrary("neuro_engine"); }
    public static native boolean initEngine(AssetManager mgr, String path, boolean vulkan);
    public static native float[] processFrame(Bitmap bitmap);
}
