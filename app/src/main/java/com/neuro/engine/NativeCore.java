package com.neuro.engine;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
public class NativeCore {
    static { System.loadLibrary("neuro_engine"); }
    public static native boolean initInferenceEngine(AssetManager mgr, String modelPath, boolean useVulkan);
    public static native float[] processFrame(Bitmap bitmap);
}
