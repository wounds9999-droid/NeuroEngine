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

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int resultCode = intent.getIntExtra("resultCode", 0);
            Intent data = intent.getParcelableExtra("data");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel chan = new NotificationChannel("V", "Engine", NotificationManager.IMPORTANCE_HIGH);
                getSystemService(NotificationManager.class).createNotificationChannel(chan);
                Notification notif = new Notification.Builder(this, "V")
                    .setContentTitle("NeuroEngine")
                    .setContentText("AI Engine is Active")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build();
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                } else {
                    startForeground(1, notif);
                }
            }

            if (data != null && mProjection == null) {
                MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                mProjection = mpm.getMediaProjection(resultCode, data);
                setupOverlay();
            }
        }
        return START_STICKY;
    }

    private void setupOverlay() {
        if (wm == null) {
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
            new Thread(() -> {
                while(mProjection != null) {
                    processFrame(overlay.getHolder().getSurface());
                    try { Thread.sleep(8); } catch(Exception e) {}
                }
            }).start();
        }
    }

    public native void processFrame(android.view.Surface s);
    @Override public IBinder onBind(Intent i) { return null; }
}
