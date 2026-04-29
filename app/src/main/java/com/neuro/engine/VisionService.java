package com.neuro.engine;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VisionService extends Service {
    private WindowManager wm;
    private SurfaceView overlay;
    private MediaProjection mProjection;
    private boolean isRunning = false;

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        
        setupNotification();
        
        int resCode = intent.getIntExtra("resCode", 0);
        Intent resData = intent.getParcelableExtra("resData");

        if (resData != null && !isRunning) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mProjection = mpm.getMediaProjection(resCode, resData);
            initOverlay();
            isRunning = true;
        }
        return START_STICKY;
    }

    private void setupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("N", "NeuroCore", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            Notification n = new Notification.Builder(this, "N")
                .setContentTitle("NeuroEngine Pro")
                .setContentText("AI Vision Core Active")
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery).build();
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

        wm.addView(overlay, p);
        new Thread(this::coreLoop).start();
    }

    private void coreLoop() {
        while (isRunning) {
            processFrame(overlay.getHolder().getSurface());
            try { Thread.sleep(8); } catch (Exception ignored) {}
        }
    }

    public native void processFrame(android.view.Surface s);
    @Override public IBinder onBind(Intent intent) { return null; }
}
