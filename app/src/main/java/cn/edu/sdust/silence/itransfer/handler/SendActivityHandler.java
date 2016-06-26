package cn.edu.sdust.silence.itransfer.handler;

import android.os.Handler;
import android.os.Message;

import cn.edu.sdust.silence.itransfer.activity.SendActivity;

/**
 * Created by feiqishi on 2016/5/16.
 */
public class SendActivityHandler extends Handler {

    private SendActivity activity;

    public SendActivityHandler(SendActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg.what == SendActivity.PROGRESS){
            activity.freshProgress(msg.arg1);
        }
        super.handleMessage(msg);
    }
}
