package cn.edu.sdust.silence.itransfer.handler;

import android.os.Handler;
import android.os.Message;
import cn.edu.sdust.silence.itransfer.activity.ReceiveActivity;


/**
 * Created by feiqishi on 2016/5/8.
 */
public class ReceiveActivityHandler extends Handler {

    public static int TYPE_PROCESS;
    private ReceiveActivity activity;

    public ReceiveActivityHandler(ReceiveActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == TYPE_PROCESS) {
            activity.setProcess(msg.arg1);
        }

        super.handleMessage(msg);
    }
}
