package com.neuro.engine;

import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VisionService extends Service {
    private WindowManager wm;
    private SurfaceView overlay;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private volatile boolean isRunning = false;
    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotification();
        int code = intent.getIntExtra("code", -1);
        Intent data = intent.getParcelableExtra("data");
        if (data != null) {
            projection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).getMediaProjection(code, data);
            initCapture();
            initOverlay();
        }
        return START_STICKY;
    }

    private void setupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel("AI", "NeuroAI", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
            Notification n = new Notification.Builder(this, "AI")
                .setContentTitle("NEURO AI ONLINE")
                .setContentText("Scanning for Targets...")
                .setSmallIcon(android.R.drawable.ic_menu_view).build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) 
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
    }

    private void initCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        imageReader = ImageReader.newInstance(metrics.widthPixels/2, metrics.heightPixels/2, PixelFormat.RGBA_8888, 2);
        projection.createVirtualDisplay("NeuroScan", metrics.widthPixels/2, metrics.heightPixels/2, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
    }

    private void initOverlay() {
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
            @Override public void surfaceCreated(SurfaceHolder h) {
                isRunning = true;
                new Thread(() -> {
                    Bitmap bmp = Bitmap.createBitmap(imageReader.getWidth(), imageReader.getHeight(), Bitmap.Config.ARGB_8888);
                    while (isRunning) {
                        nativeRender(h.getSurface(), bmp);
                        try { Thread.sleep(10); } catch (Exception ignored) {}
                    }
                }).start();
            }
            @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) {}
            @Override public void surfaceDestroyed(SurfaceHolder h) { isRunning = false; }
        });
        wm.addView(overlay, p);
    }

    public native void nativeRender(Surface s, Bitmap b);
    @Override public IBinder onBind(Intent i) { return null; }
}
