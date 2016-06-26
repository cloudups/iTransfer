package cn.edu.sdust.silence.itransfer.util;

/**
 * Created by feiqishi on 2016/5/16.
 */
public class Log {
    private static String TAG = "xyz";

    public static void log(String log) {
        android.util.Log.i(TAG, log);
    }
}
