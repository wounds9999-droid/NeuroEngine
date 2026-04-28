package com.neuro.engine;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NativeCore core = new NativeCore();
        // سيتم ربط المسارات لاحقاً
    }
}
