package com.neuro.engine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VisionService extends Service {
    private WindowManager wm;
    private SurfaceView overlay;

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // إنشاء إشعار الخدمة الخلفية
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("V", "Vision", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            Notification notif = new Notification.Builder(this, "V").setContentTitle("NeuroEngine Active").build();
            
            // السر هنا: إضافة ختم أندرويد 14 عشان ما يكرش
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } else {
                startForeground(1, notif);
            }
        }

        // تحميل الذكاء الاصطناعي وإعداد الشاشة
        if (wm == null) {
            loadModel(getAssets());
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            overlay = new SurfaceView(this);
            overlay.setZOrderOnTop(true);
            overlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);

            WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );

            overlay.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override public void surfaceCreated(SurfaceHolder h) { startLoop(h.getSurface()); }
                @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) {}
                @Override public void surfaceDestroyed(SurfaceHolder h) {}
            });
            wm.addView(overlay, p);
        }
        return START_STICKY;
    }

    private void startLoop(android.view.Surface s) {
        new Thread(() -> {
            float enemyX = 1000f; 
            float enemyY = 500f;
            while(true) {
                processFrame(s, enemyX, enemyY, 150f, 300f); 
                try { Thread.sleep(16); } catch(Exception e) {} 
            }
        }).start();
    }

    public native boolean loadModel(AssetManager mgr);
    public native void processFrame(android.view.Surface s, float x, float y, float w, float h);

    @Override public IBinder onBind(Intent i) { return null; }
}
