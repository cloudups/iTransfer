package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import cn.edu.sdust.silence.itransfer.handler.SendActivityHandler;

/**
 * 发送文件子线程管理线程
 *
 * Created by feiqishi on 2016/5/15.
 */
public class ServerManager2 extends Thread {

    private String ip;
    private String filePath;
    private Handler managerHandler;
    private SendActivityHandler sendActivityHandler;

    public static int RETRY = 1;
    public static int FINISH = 2;

    public ServerManager2(SendActivityHandler sendActivityHandler, String ip, String filePath) {
        this.sendActivityHandler = sendActivityHandler;
        this.ip = ip;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        Looper.prepare();
        managerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == RETRY) {
                    DataServerThread2 server = new DataServerThread2(sendActivityHandler,managerHandler, ip, filePath);
                    server.start();
                } else if (msg.what == FINISH) {
                    managerHandler.getLooper().quit();
                    Thread.interrupted();
                }
            }
        };
        DataServerThread2 server = new DataServerThread2(sendActivityHandler,managerHandler, ip, filePath);
        server.start();
        Looper.loop();

    }
}
