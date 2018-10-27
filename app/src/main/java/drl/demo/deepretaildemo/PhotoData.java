package drl.demo.deepretaildemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by root on 17.10.17.
 */

public class PhotoData {
    //TODO: make data private
    public byte[] data;
    public String address = "";
    public int imgwidth = 350;
    public int imgHeight = 450;
    public int imgCount = 0;
    public String className = "NOclassName";

    public PhotoData(byte[] raw){
        //TODO: implement converson from raw (video) data
        this.data = raw;
    }
    public PhotoData(Bitmap photo){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        this.data = stream.toByteArray();
    }

    public byte[] getDownscaled(){
        Bitmap original = BitmapFactory.decodeByteArray(this.data , 0, this.data.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, this.imgwidth, this.imgHeight, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 40, blob);

        return blob.toByteArray();
    }
}
