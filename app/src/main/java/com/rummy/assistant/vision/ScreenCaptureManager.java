package com.rummy.assistant.vision;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;

public class ScreenCaptureManager {
    private static final String TAG = "ScreenCaptureManager";
    private final Context context;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface ScreenCaptureListener {
        void onFrameCaptured(Bitmap bitmap);
        void onError(String error);
    }

    public ScreenCaptureManager(Context context) {
        this.context = context;
    }

    public Intent getProjectionIntent() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        return projectionManager.createScreenCaptureIntent();
    }

    public void startCapture(int resultCode, Intent data, ScreenCaptureListener listener) {
        MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);

        // Capture a single frame for now (as requested for user-initiated workflow)
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int density = context.getResources().getDisplayMetrics().densityDpi;

        imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("RummyCapture",
                width, height, density,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Bitmap bitmap = imageToBitmap(image);
                image.close();
                if (bitmap != null) {
                    handler.post(() -> listener.onFrameCaptured(bitmap));
                } else {
                    handler.post(() -> listener.onError("Failed to convert image to bitmap"));
                }
            }
        }, handler);
    }

    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int stride = planes[0].getRowStride();
        int pixelStride = planes[0].getPixelStride();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];

        int bufferPosition = buffer.position();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // RGBA_8888
                int r = buffer.get(bufferPosition) & 0xFF;
                int g = buffer.get(bufferPosition + 1) & 0xFF;
                int b = buffer.get(bufferPosition + 2) & 0xFF;
                int a = buffer.get(bufferPosition + 3) & 0xFF;
                pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
                bufferPosition += pixelStride;
            }
            bufferPosition = bufferPosition - (width * pixelStride) + stride;
        }
        buffer.position(0);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void stopCapture() {
        if (virtualDisplay != null) virtualDisplay.release();
        if (imageReader != null) imageReader.close();
        if (mediaProjection != null) mediaProjection.stop();
    }
}
