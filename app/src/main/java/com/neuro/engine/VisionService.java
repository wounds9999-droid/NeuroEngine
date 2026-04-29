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
    private boolean isEngineRunning = false;

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("V", "Vision", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            Notification notif = new Notification.Builder(this, "V").setContentTitle("NeuroEngine Active").build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } else {
                startForeground(1, notif);
            }
        }

        if (!isEngineRunning) {
            loadModel(getAssets());
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            overlay = new SurfaceView(this);
            overlay.setZOrderOnTop(true);
            overlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);

            // تحديد أبعاد الشاشة لمنع تعارض النظام
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();

            WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                width, height,
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
            isEngineRunning = true;
        }
        return START_STICKY;
    }

    private void startLoop(android.view.Surface s) {
        new Thread(() -> {
            float enemyX = 500f; 
            float enemyY = 500f;
            while(true) {
                processFrame(s, enemyX, enemyY, 200f, 400f); 
                try { Thread.sleep(16); } catch(Exception e) {} 
            }
        }).start();
    }

    public native boolean loadModel(AssetManager mgr);
    public native void processFrame(android.view.Surface s, float x, float y, float w, float h);

    @Override public IBinder onBind(Intent i) { return null; }
}
