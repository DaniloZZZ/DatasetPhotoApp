package drl.demo.deepretaildemo;

import android.animation.TypeEvaluator;
import android.util.Log;

public class heightEvaluator implements TypeEvaluator {
    public Object evaluate(float fraction, Object from,Object to){
        Log.i("DS",String.valueOf(fraction)+" f:"+String.valueOf(from)+" t:"+String.valueOf(to));
        return String.valueOf((int)fraction) + "dp";
    }
}
