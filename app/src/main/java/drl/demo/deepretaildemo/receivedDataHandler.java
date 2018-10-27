package drl.demo.deepretaildemo;

import android.util.Log;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Danil on 19.10.17.
 */

public class receivedDataHandler {
    InputStream stream;
    String TAG ="READ";
    int MAX_IMG = 20*1024;
    public  receivedDataHandler(InputStream s){
       this.stream = s;
    }

    public List<byte[]> parseImages(){
        // using DEAFBEEF as default delimeter
        byte[] delimeter =  new String("newdrtimg").getBytes();
        byte[] endheader = {0x0d,0x0a,0x0d,0x0a}; // \r\n\r\n is end of header part

        //find headers of response
        String headers = new String(readuntill(endheader));
        Log.i(TAG,"received Headers:"+headers);
        Log.i("parse","using delimeter:"+ new String(delimeter));

        // process the rest until connection is closed
        List<byte[]> list = new LinkedList<byte[]>() ;
        try {
            while (this.stream.available() > 0) {
                Log.i(TAG,"reading untill");
                byte[] img = readuntill(delimeter);
                list.add(img);
                Log.i(TAG, "adding " + String.valueOf(img.length) + " bytes to array");
            }
        }catch (Exception e){
            Log.e(TAG,e.getStackTrace().toString());
        }
        return list;
    }

    private void pushoff(byte[] cont, int b) {
        for(int i=0;i<cont.length-1;i++){
            cont[i]= cont[i+1];
        }
        cont[cont.length-1] = (byte) b;
    }

    private byte[] readuntill(byte[] pattern) {
        int b;
        byte[] bufferimg = new byte[this.MAX_IMG];
        byte[] cont = new byte[pattern.length];
        int imp = 0;
        try {
            while (!isMatch(cont, pattern, 0)) {
                // push to container, may be an error id datalength is lessd than delimeter
                if ((b = this.stream.read())>-1) {
                    pushoff(cont, b);
                    bufferimg[imp] = (byte) b;
                    imp++;
                    if (imp == this.MAX_IMG) {
                        Log.e(TAG, "Exceeded max buffer size");
                        return null;
                    }
                }else {
                    return bufferimg;
                }
            }
            Log.i(TAG, "found match in pos" + String.valueOf(imp));
            return Arrays.copyOfRange(bufferimg, 0, imp - pattern.length);

        } catch (Exception e) {
            Log.e(TAG, e.getStackTrace().toString());
            return null;
        }
    }
    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }
}

