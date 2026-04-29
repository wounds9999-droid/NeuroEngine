package com.neuro.engine;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager mpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Button btn = findViewById(R.id.startBtn);
        
        btn.setOnClickListener(v -> {
            startActivityForResult(mpm.createScreenCaptureIntent(), 300);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) {
            Intent i = new Intent(this, VisionService.class);
            i.putExtra("resCode", resultCode);
            i.putExtra("resData", data);
            startForegroundService(i);
            moveTaskToBack(true);
        }
    }
}
