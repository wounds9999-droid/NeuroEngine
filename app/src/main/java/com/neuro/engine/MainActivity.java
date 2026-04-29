package com.neuro.engine;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);

        checkOverlayPermission();
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                statusText.setText("مطلوب إذن الظهور فوق التطبيقات (Overlay)");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            } else {
                engineReady();
            }
        } else {
            engineReady();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    engineReady();
                } else {
                    statusText.setText("تم رفض الإذن! لا يمكن رسم الرادار.");
                    statusText.setTextColor(0xFFFF0000);
                }
            }
        }
    }

    private void engineReady() {
        statusText.setText("Overlay Granted\n[ Vision System Ready ]\nجاهز لتصوير الشاشة");
        statusText.setTextColor(0xFF00FF00);
    }
}
