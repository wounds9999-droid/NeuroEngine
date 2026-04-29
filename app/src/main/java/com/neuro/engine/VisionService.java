package com.neuro.engine;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VisionService extends Service {
    private MediaProjection projection;
    private ImageReader reader;
    private SurfaceView overlay;
    private WindowManager wm;
    private boolean isRunning = true;

    static { System.loadLibrary("neuro_engine"); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int code = intent.getIntExtra("code", -1);
        Intent data = intent.getParcelableExtra("data");
        
        projection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).getMediaProjection(code, data);
        
        DisplayMetrics dm = new DisplayMetrics();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(dm);

        // إنشاء "العين" اللي تصور اللعبة بدقة 720p للسرعة
        reader = ImageReader.newInstance(1280, 720, PixelFormat.RGBA_8888, 2);
        projection.createVirtualDisplay("NeuroScan", 1280, 720, dm.densityDpi, 
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);

        setupOverlay(dm.widthPixels, dm.heightPixels);
        return START_STICKY;
    }

    private void setupOverlay(int w, int h) {
        overlay = new SurfaceView(this);
        overlay.setZOrderOnTop(true);
        overlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(w, h,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 2038 : 2003,
            8, PixelFormat.TRANSLUCENT);

        wm.addView(overlay, p);

        new Thread(() -> {
            Bitmap bmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888);
            while(isRunning) {
                Image img = reader.acquireLatestImage();
                if (img != null) {
                    // تحويل صورة اللعبة إلى بيانات يفهمها المحرك
                    nativeRender(overlay.getHolder().getSurface(), bmp);
                    img.close();
                }
                try { Thread.sleep(16); } catch(Exception e) {}
            }
        }).start();
    }

    public native void nativeRender(Surface s, Bitmap b);
    @Override public IBinder onBind(Intent i) { return null; }
}
