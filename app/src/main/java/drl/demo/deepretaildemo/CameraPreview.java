package drl.demo.deepretaildemo;

/**
 * Created by root on 17.10.17.
 */

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final MainActivity mact;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG  = "camPreview";

    private int num_prew;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mact = (MainActivity) context;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    boolean isFirstTime = true;
    long startTime = 0;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isFirstTime) {
            isFirstTime = false;
            startTime = SystemClock.currentThreadTimeMillis();
        }else {
            long currentTime = SystemClock.currentThreadTimeMillis();
            long elapsedTime = currentTime - startTime;
            Log.i(TAG,"got preview "+elapsedTime); Camera.Parameters parameters = camera.getParameters();
            if (elapsedTime >= 200) { // trigger your event
                startTime = currentTime;
                int imageFormat = parameters.getPreviewFormat();
                if (imageFormat == ImageFormat.NV21)
                {
                    num_prew=0;
                    try
                    {
                        int pich = camera.getParameters().getPreviewSize().height;
                        int picw = camera.getParameters().getPreviewSize().width;
                        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,picw, pich, null);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(new Rect(0, 0, picw, pich), 100, os);
                        byte[] jpegByteArray = os.toByteArray();

                        PhotoBatch batch = new PhotoBatch();
                        batch.address = mact.textOf(R.id.ipText);
                        batch.address = "http://"+mact.textOf(R.id.ipText)+":"+mact.textOf(R.id.portNum);
                        batch.type = "predict";

                        PhotoData photo = new PhotoData(jpegByteArray);
                        batch.add(photo);
                        Log.i(TAG,"sending image");
                        mact.sendList(batch);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
        }
        }
    };

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

}