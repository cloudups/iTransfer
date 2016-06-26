package cn.edu.sdust.silence.itransfer.handler;

import android.os.Handler;
import android.os.Message;
import java.util.List;
import cn.edu.sdust.silence.itransfer.activity.ReceiveFromComputerActivity;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;


/**
 * Created by feiqishi on 2016/5/8.
 */
public class ReceiveFromComputerActivityHandler extends Handler {

    public static int TYPE_LIST_SUCCESS = 1;
    public static int TYPE_LIST_FAIL = 2;
    public static int TYPE_DOWNLOAD_SUCCESS = 3;
    public static int TYPE_DOWNLOAD_FAIL = 4;
    private ReceiveFromComputerActivity activity;

    public ReceiveFromComputerActivityHandler(ReceiveFromComputerActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == TYPE_LIST_SUCCESS) {
            List<FileLog> files = (List<FileLog>) msg.obj;
            activity.listSuccess(files);
        } else if (msg.what == TYPE_LIST_FAIL) {
            activity.listFail();
        } else if (msg.what == TYPE_DOWNLOAD_SUCCESS) {
            activity.downloadSuccess();
        } else if (msg.what == TYPE_DOWNLOAD_FAIL) {
            activity.downloadFail();
        }
        super.handleMessage(msg);
    }
}
