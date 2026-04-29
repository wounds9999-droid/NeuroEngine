package com.neuro.engine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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

    // استدعاء محرك الذكاء الاصطناعي C++
    static { System.loadLibrary("neuro_engine"); }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // إبقاء التطبيق شغال في الخلفية
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("V", "Vision", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            Notification notif = new Notification.Builder(this, "V")
                .setContentTitle("NeuroEngine")
                .setContentText("الرادار يعمل الآن...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
            startForeground(1, notif);
        }

        // تحميل أوزان الذكاء الاصطناعي
        loadModel(getAssets());

        // إعداد الشاشة الشفافة فوق ببجي
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

    private void startLoop(android.view.Surface s) {
        new Thread(() -> {
            while(true) {
                processFrame(s);
                try { Thread.sleep(8); } catch(Exception e) {} // 120 FPS
            }
        }).start();
    }

    // دوال الربط مع C++
    public native boolean loadModel(AssetManager mgr);
    public native void processFrame(android.view.Surface s);

    @Override public IBinder onBind(Intent i) { return null; }
}
