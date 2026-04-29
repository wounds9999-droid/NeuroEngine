package com.neuro.engine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusText = findViewById(R.id.statusText);

        // طلب صلاحية الكاميرا أول ما يفتح التطبيق
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startEngine();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startEngine();
        } else {
            statusText.setText("تم رفض الصلاحية!\nالمحرك يحتاج الكاميرا للعمل.");
            statusText.setTextColor(0xFFFF0000); // لون أحمر
        }
    }

    private void startEngine() {
        statusText.setText("NeuroEngine: ACTIVE\n[ C++ Core Linked ]\nجاهز للربط مع ببجي");
    }
}
