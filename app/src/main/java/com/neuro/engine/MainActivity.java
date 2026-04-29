package com.neuro.engine;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager mpm;
    private ProgressBar loader;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        loader = findViewById(R.id.loader);
        startBtn = findViewById(R.id.startBtn);
        
        startBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            } else {
                triggerCapture();
            }
        });
    }

    private void triggerCapture() {
        startBtn.setEnabled(false);
        loader.setVisibility(View.VISIBLE);
        startActivityForResult(mpm.createScreenCaptureIntent(), 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            Intent service = new Intent(this, VisionService.class);
            service.putExtra("code", resultCode);
            service.putExtra("data", data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
            getWindow().getDecorView().postDelayed(() -> moveTaskToBack(true), 1000);
        } else {
            startBtn.setEnabled(true);
            loader.setVisibility(View.INVISIBLE);
        }
    }
}
