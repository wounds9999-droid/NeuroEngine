package com.neuro.engine;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager mpm;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            status.setText("GRANT OVERLAY PERMISSION...");
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 200);
        } else {
            requestCapture();
        }
    }

    private void requestCapture() {
        status.setText("WAITING FOR CAPTURE PERMISSION...");
        startActivityForResult(mpm.createScreenCaptureIntent(), 300);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) { requestCapture(); }
        else if (requestCode == 300 && resultCode == RESULT_OK) {
            status.setText("CORE ACTIVE - DEPLOYING ENGINE");
            Intent i = new Intent(this, VisionService.class);
            i.putExtra("resCode", resultCode);
            i.putExtra("resData", data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
            status.postDelayed(() -> moveTaskToBack(true), 1500);
        }
    }
}
