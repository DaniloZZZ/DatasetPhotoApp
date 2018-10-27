package drl.demo.deepretaildemo;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by root on 20.10.17.
 */

public class VideoRecorder {
    public int framerate = 24;
    public boolean isrec = false;
    private Camera cam;
    private SurfaceView surf;
    private MediaRecorder mrec;
    private File vFile;

    public VideoRecorder(Camera c,SurfaceView sv){
        this.cam = c;
        this.surf =sv;
    }
    public boolean start(){
        if(!this.isrec){
            if(prepareVideoRecorder()){
                this.mrec.start();
                this.isrec = true;
                return  true;
            }else {
                this.isrec = false;
                releaseMediaRecorder();
                return false;
            }
        }else {return false;}
    }
    public File stop(){
       if(this.isrec){
           // stop recording and release camera
           this.mrec.stop();  // stop the recording
           releaseMediaRecorder(); // release the MediaRecorder object
           this.cam.lock();         // take camera access back from MediaRecorder
           this.isrec = false;
           return vFile;
       }else {return vFile;}
    }
    private boolean prepareVideoRecorder(){

        Camera mCamera = this.cam;
        MediaRecorder mMediaRecorder = new MediaRecorder();
        this.mrec= mMediaRecorder;

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // Step 4: Set output file
        vFile  = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(vFile.toString());
        mMediaRecorder.setVideoFrameRate(this.framerate);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(this.surf.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private void releaseMediaRecorder(){
        if (this.mrec!= null) {
            this.mrec.reset();   // clear recorder configuration
            this.mrec.release(); // release the recorder object
            this.mrec= null;
            this.cam.lock();           // lock camera for later use
        }
    }
    private void releaseCamera(){
        if (this.cam!= null){
            this.cam.release();        // release the camera for other applications
            this.cam= null;
        }
    }
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DeepRetail");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
