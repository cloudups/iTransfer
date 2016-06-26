package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.sdust.silence.itransfer.handler.ReceiveActivityHandler;

/**
 * 接收文件子线程管理线程
 *
 * Created by feiqishi on 2016/5/15.
 */
public class ReceiveManager extends Thread {

    private ReceiveActivityHandler receiveActivityHandler;
    private ServerSocket serverSocket;
    private Socket socket = null;
    private Handler managerHandler;

    public static int FINISH = 1;
    public static int RETRY = 2;

    public ReceiveManager(ReceiveActivityHandler handler) {
        this.receiveActivityHandler = handler;
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        managerHandler = new Handler() {

            public void handleMessage(Message msg) {

                if(msg.what == RETRY){
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    DataReceiveThread2 thread = new DataReceiveThread2(managerHandler, receiveActivityHandler, socket);
                    thread.start();
                }
                if (msg.what == FINISH) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    managerHandler.getLooper().quit();
                    Thread.interrupted();
                }
            }
        };

        try {
            socket = serverSocket.accept();
            DataReceiveThread2 thread = new DataReceiveThread2(managerHandler, receiveActivityHandler, socket);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Looper.loop();
    }

    @Override
    public void destroy() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}
