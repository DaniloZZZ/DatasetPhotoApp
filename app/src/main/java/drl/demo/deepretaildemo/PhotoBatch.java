package drl.demo.deepretaildemo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by root on 21.10.17.
 */

public class PhotoBatch {
    public java.lang.String type= "none";
    List<PhotoData> list = new LinkedList<>();
    int size =  0;
    String className = "NOclassName";
    String vidId = "0";
    String address ;
    public PhotoBatch(List<PhotoData> l){
        this.list = l;
        this.size = l.size();
    }

    public PhotoBatch(){
       this.list = new LinkedList<>();
        this.size = 0;
    }

    public PhotoData get(int i){
        return this.list.get(i);
    }

    public void add(PhotoData d){
        this.list.add(d);
        this.size++;
    }

}
