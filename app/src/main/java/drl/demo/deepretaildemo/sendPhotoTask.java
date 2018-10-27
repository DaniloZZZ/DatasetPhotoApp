package drl.demo.deepretaildemo;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Danil on 17.10.17.
 */

public class sendPhotoTask extends AsyncTask<PhotoBatch,Void,String > {

    private String TAG = "SendPhotoTask";
    private MainActivity.photoSentClb onFinished;
    private boolean waserror;
    private byte[] receivedBlob ={};
    private List<byte[]> imgList;
    private String type;
    private String preds = "none";

    public sendPhotoTask(MainActivity.photoSentClb sentClb) {
        // initiate instance of task with callback interface
        this.onFinished = sentClb;
    }

    protected String doInBackground(PhotoBatch... photoBatches){
        HttpClient httpclient = new DefaultHttpClient();
        PhotoBatch batch = photoBatches[0];
        Log.i(TAG,batch.size+" -  length of franmes list");
        HttpPost httppost = new HttpPost(batch.address);
        try {
            this.type = batch.type;
            // Form multipart data to send to server
            MultipartEntity mpEntity = new MultipartEntity();
            mpEntity.addPart("dataType",new StringBody(batch.type));
            mpEntity.addPart("imgCount",new StringBody(String.valueOf(batch.size)));
            mpEntity.addPart("setName", new StringBody("debug"));
            mpEntity.addPart("vidId",new StringBody(batch.vidId));
            mpEntity.addPart("className", new StringBody(batch.className));
            for(int i =0; i<photoBatches[0].size; i++) {
                mpEntity.addPart("image", new ByteArrayBody(batch.get(i).getDownscaled(), "image/jpeg", "test2.jpg"));
            }
            httppost.setEntity(mpEntity);

            // Send post request
            HttpResponse response = httpclient.execute(httppost);
            InputStream inputstream = response.getEntity().getContent();
            if(this.type=="train") {
                // perform receveing and splitting of data
                this.imgList = new receivedDataHandler(inputstream).parseImages();
                Log.i(TAG, "RESPONSE:" + String.valueOf(this.receivedBlob));
                this.waserror = false;
                return "Finished with " + this.imgList.size() + " images";
            }else{
                byte[] arr =  new byte[256];
                inputstream.read(arr);

                this.preds = new String(arr,"UTF-8");
                Log.i("raw",this.preds);
                String regex = "\\[(.+?)\\]";
                Pattern p = Pattern.compile(regex);
                Matcher matcher = p.matcher(this.preds);
                if(matcher.find()) {
                    this.preds= matcher.group(1);
                }
                return "predicted label:"+this.preds;
            }

        } catch (final Exception e) {
            this.waserror=  true;
            Log.e(TAG, "Error while sending request: " + e);
            return e.getMessage();
        }
    }

    protected void onPostExecute(String result)
    {
        try {
            if(this.waserror){
                this.onFinished.say(result,1);
            }
            else {
                if(this.type=="train") {
                    this.onFinished.handleData(this.imgList);
                    this.onFinished.say("Received  bytes", 0);
                }else {
                    this.onFinished.handlePredictions(this.preds);
                }
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
        Log.i(TAG,"send Photo Task ended"+result);
    }
}
