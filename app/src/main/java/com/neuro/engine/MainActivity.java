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
    private TextView statusText;
    private MediaProjectionManager mpm;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        checkPermissions();
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            statusText.setText("مطلوب إذن الظهور فوق التطبيقات");
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 200);
        } else {
            statusText.setText("مطلوب إذن تصوير الشاشة للرادار");
            startActivityForResult(mpm.createScreenCaptureIntent(), 300);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            checkPermissions();
        } else if (requestCode == 300 && resultCode == RESULT_OK) {
            statusText.setText("NeuroEngine: ACTIVE\nالعب وانبسط!");
            Intent intent = new Intent(this, VisionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
        } else {
            statusText.setText("تم رفض الصلاحيات!");
            statusText.setTextColor(0xFFFF0000);
        }
    }
}
