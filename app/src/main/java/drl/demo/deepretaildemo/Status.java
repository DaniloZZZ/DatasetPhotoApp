package drl.demo.deepretaildemo;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class Status {
    private TextView txtbox;
    private int green = Color.GREEN;
    private int red =Color.RED;
    private int blue = Color.BLUE;
    Status(MainActivity act){
        this.txtbox = (TextView) act.findViewById(R.id.statusText);
        Log.i("statusinit:", (String) this.txtbox.getText());
    }
   public void set(String msg){
       Log.i(TAG,msg);
       this.txtbox.setTextColor(green);
       this.txtbox.setText(msg);
    }

    public void err(String s) {
        Log.e(TAG,s);
        this.txtbox.setTextColor(red);
        this.txtbox.setText(s);
    }
}
