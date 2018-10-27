package drl.demo.deepretaildemo;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Danil on 20.10.17.
 */
;


public class VideoSplitter {

    public static final double SECONDS_BETWEEN_FRAMES = 10;

    private static final String inputFilename = "c:/Java_is_Everywhere.mp4";
    private static final String outputFilePrefix = "c:/snapshots/mysnapshot";
    private static final String TAG = "Vspl";

    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    public int cnt;
    public int dur;


    List<Bitmap> split(File mediaFile) {
                List<Bitmap> list = new LinkedList<>();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                AndroidFrameConverter bmpConv = new AndroidFrameConverter();
                retriever.setDataSource(mediaFile.getAbsolutePath());
                int dt = dur/cnt;
                Log.i("spl",dur+"a"+cnt+"b");

                for (int i = 0; i < cnt; i++) {
                    list.add(retriever.getFrameAtTime(dt * i * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC));
                        /*FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(mediaFile.getAbsolutePath());
                                Frame img;
                                try {
                                    frameGrabber.start();
                                    frameGrabber.setFrameNumber(i * 20);
                                    img = frameGrabber.grab();
                                    list.add(bmpConv.convert(img));
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                        */
                }
                return list;
        }
}
