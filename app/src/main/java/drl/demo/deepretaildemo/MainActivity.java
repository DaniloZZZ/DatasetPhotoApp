package drl.demo.deepretaildemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Camera mCamera;
    private VideoRecorder vRec;
    private Status status;
    private boolean settingsBarOpen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"Hello there!"+R.layout.activity_main);

        status = new Status(this);
        Log.i(TAG,"initialized Status");
        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        vRec = new VideoRecorder(mCamera,mPreview);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        Log.i(TAG,"Capture button pressed");
                        status.set("Recording...");
                        vRec.start();
                        int vidDur = Integer.parseInt( ((TextView) findViewById(R.id.vidDurationText)).getText().toString());
                        try
                        {
                            Thread.sleep(vidDur*1000);
                        }
                        catch(InterruptedException ex)
                        {
                            Thread.currentThread().interrupt();
                        }
                        File record = vRec.stop();
                        VideoSplitter vs =  new VideoSplitter();
                        vs.dur = vidDur*1000;
                        vs.cnt = vidDur; //// FIXME: 21.10.17 now we can grab only one per second
                        PhotoBatch batch = new PhotoBatch();
                        List<Bitmap> bmpl = vs.split(record);

                        batch.address = textOf(R.id.ipText);
                        batch.className = textOf(R.id.classNameText);
                        batch.vidId = textOf(R.id.phCountText); // // FIXME: 21.10.17 using created field coz lazy
                        batch.address = "http://"+textOf(R.id.ipText)+":"+textOf(R.id.portNum);
                        batch.type = "train";

                        for (int i = 0;i<bmpl.size();i++){
                            PhotoData d = new PhotoData(bmpl.get(i));
                            batch.add(d);
                        }
                        Log.i(TAG,"splitted to "+bmpl.size()+" farmes");

                        status.set("Recording ended.");

                        sendList(batch);
                        //mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
       // navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        status.set("init ended");
    }

    private String TAG = "mainact";
    private List<PhotoData> myDataList;
    private int num_prew= 0;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            PhotoData myData = new PhotoData(data);
            //TODO: dont hardcode address
            TextView addr = (TextView) findViewById(R.id.ipText);
            myData.address="http://"+addr.getText()+":8899";
            status.set("Photo taken");
            camera.startPreview();
            Log.i(TAG,"Prewiew started");

            Log.i(TAG,"exec called");

        }
    };
    public void sendList(PhotoBatch myDataList){
        sendPhotoTask photoTask = new sendPhotoTask(new photoSentClb(){
            @Override
            public void say(String msg, int code) {
                if (code == 0) {
                    status.set(msg);
                } else {
                    status.err(msg);
                }
            }
            @Override
            public void handleData(List<byte[]> data) {
                onDataReceived(data);
                return;
            }
            @Override
            public void  handlePredictions(String  preds){
                status.set("label: "+preds);
            }
        });
        photoTask.execute(myDataList);
    }

    private void onDataReceived(List<byte[]> images) {
        try {
            status.set("Setting image to view");
            Log.i(TAG,"parsed images"+String.valueOf(images.get(0)));
            ImageView iv = (ImageView) findViewById(R.id.imageView);
            ImageView iv2 = (ImageView) findViewById(R.id.imageView2);
            Bitmap bitmap = BitmapFactory.decodeByteArray(images.get(0), 0, images.get(0).length);
            Bitmap bitmap2 = BitmapFactory.decodeByteArray(images.get(1), 0, images.get(1).length);
            iv.setImageBitmap(bitmap);
            iv2.setImageBitmap(bitmap2);
        }
        catch(Exception e) {
            status.err(e.getMessage());
            Log.e(TAG,"error while handling receieved "+e.toString());
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            status.err("Failed to init camera:"+e.getLocalizedMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public interface photoSentClb{
        public int code = 0;
        public InputStream result = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };

        void say(String msg,int code);
        void handleData(List<byte[]> data);
        void handlePredictions(String preds);
    }

    public String textOf(int id){
        return ((TextView) findViewById(id)).getText().toString();
    }

    public void toggleSettingsBar(View view) {
        RelativeLayout settingsBar = (RelativeLayout) findViewById(R.id.settingWrapper);
        if(this.settingsBarOpen) {
            ValueAnimator open = ObjectAnimator.ofFloat(settingsBar, "scaleY", 1f);
            open.setDuration(300);
            open.start();

        }else {
            ValueAnimator close = ObjectAnimator.ofFloat(settingsBar, "scaleY", 0f);
            close.setDuration(300);
            close.start();
        }
        this.settingsBarOpen =! this.settingsBarOpen;
    }
}
