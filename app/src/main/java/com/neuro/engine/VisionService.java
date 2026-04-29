package com.neuro.engine;
import android.app.*;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;

public class VisionService extends Service {
    private WindowManager wm;
    private SurfaceView overlay;
    static { System.loadLibrary("neuro_engine"); }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel("V", "Vision", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
            startForeground(1, new NotificationCompat.Builder(this, "V").setContentTitle("NeuroEngine Active").setContentText("Radar Online").setSmallIcon(android.R.drawable.ic_dialog_info).build());
        }

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

    private void startLoop(android.view.Surface s) {
        new Thread(() -> {
            while(true) {
                processFrame(s);
                try { Thread.sleep(8); } catch(Exception e) {}
            }
        }).start();
    }

    public native boolean loadModel(AssetManager mgr);
    public native void processFrame(android.view.Surface s);
    @Override public IBinder onBind(Intent i) { return null; }
}
