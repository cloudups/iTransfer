package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import cn.edu.sdust.silence.itransfer.handler.ReceiveActivityHandler;

/**
 * 接收文件子线程管理线程
 *
 * Created by feiqishi on 2016/5/15.
 */
public class ReceiveManager2 extends Thread {

    public static int RETRY = 01;
    public static int FINISH = 02;
    private Handler managerHandler;
    private ReceiveActivityHandler receiveActivityHandler;
    private String ip;

    public ReceiveManager2(ReceiveActivityHandler receiveActivityHandler, String ip) {
        this.receiveActivityHandler = receiveActivityHandler;
        this.ip = ip;
    }

    @Override
    public void run() {

        Looper.prepare();
        managerHandler = new Handler() {

            public void handleMessage(Message msg) {

                if (msg.what == RETRY) {
                    DataReceiveThread thread = new DataReceiveThread(managerHandler, receiveActivityHandler, ip);
                    thread.start();
                } else if (msg.what == FINISH) {
                    managerHandler.getLooper().quit();
                    Thread.interrupted();
                }
            }
        };

        DataReceiveThread thread = new DataReceiveThread(managerHandler, receiveActivityHandler, ip);
        thread.start();
        Looper.loop();
    }
}
