package com.neuro.engine;

public class NativeCore {
    static {
        System.loadLibrary("neuro_engine");
    }
    public native void initVulkan(String paramPath, String binPath);
}
