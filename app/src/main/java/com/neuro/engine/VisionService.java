package com.neuro.engine;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VisionService extends Service {
    private WindowManager wm;
    private SurfaceView overlay;
    private MediaProjection projection;
    private volatile boolean isRunning = false;
    private Thread renderThread;

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        setupNotification();
        
        int code = intent.getIntExtra("code", -1);
        Intent data = intent.getParcelableExtra("data");

        if (data != null && !isRunning) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            projection = mpm.getMediaProjection(code, data);
            initOverlay();
        }
        return START_STICKY;
    }

    private void setupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel("U", "UltraCore", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
            Notification n = new Notification.Builder(this, "U")
                .setContentTitle("NEURO ENGINE ACTIVE")
                .setContentText("Neural Vision Stream Online")
                .setSmallIcon(android.R.drawable.ic_dialog_info).build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } else {
                startForeground(1, n);
            }
        }
    }

    private void initOverlay() {
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
            @Override
            public void surfaceCreated(SurfaceHolder h) {
                isRunning = true;
                renderThread = new Thread(() -> {
                    Surface s = h.getSurface();
                    while (isRunning) {
                        nativeRender(s);
                        try { Thread.sleep(8); } catch (Exception e) { Thread.currentThread().interrupt(); }
                    }
                });
                renderThread.setPriority(Thread.MAX_PRIORITY);
                renderThread.start();
            }
            @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) {}
            @Override public void surfaceDestroyed(SurfaceHolder h) { isRunning = false; }
        });
        wm.addView(overlay, p);
    }

    public native void nativeRender(Surface s);
    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (wm != null && overlay != null) wm.removeView(overlay);
        if (projection != null) projection.stop();
        super.onDestroy();
    }
}
