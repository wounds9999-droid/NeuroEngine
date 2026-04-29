package com.neuro.engine;
import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class VisionService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("V", "Vision", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            startForeground(1, new NotificationCompat.Builder(this, "V").setContentTitle("NeuroEngine Active").setSmallIcon(android.R.drawable.ic_menu_view).build());
        }
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
