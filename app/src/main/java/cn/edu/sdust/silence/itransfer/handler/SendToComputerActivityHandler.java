package cn.edu.sdust.silence.itransfer.handler;

import android.os.Handler;
import android.os.Message;

import java.util.List;

import cn.edu.sdust.silence.itransfer.activity.SendToComputerActivity;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;

/**
 * Created by feiqishi on 2016/5/16.
 */
public class SendToComputerActivityHandler extends Handler {

    private SendToComputerActivity activity;

    public SendToComputerActivityHandler(SendToComputerActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == SendToComputerActivity.TYPE_SUCCESS) {
            activity.uploadSuccess((List<FileLog>) msg.obj);
        } else if (msg.what == SendToComputerActivity.TYPE_FAIL) {
            activity.uploadFail();
        }else if(msg.what == SendToComputerActivity.TYPE_SCAN){
            activity.scanSuccess(msg.obj.toString());
        }
        super.handleMessage(msg);
    }
}
