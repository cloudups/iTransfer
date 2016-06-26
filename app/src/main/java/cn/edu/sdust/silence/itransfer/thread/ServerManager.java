package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.sdust.silence.itransfer.handler.SendActivityHandler;

/**
 * 发送文件子线程管理线程
 *
 * Created by feiqishi on 2016/5/15.
 */
public class ServerManager extends Thread {

    private String filePath;
    private ServerSocket serverSocket;
    private Socket socket;
    private int PORT = 8888;

    public static int RETRY = 1;
    public static int FINISH = 2;

    private Handler managerHandler;
    private SendActivityHandler sendActivityHandler;

    public ServerManager(SendActivityHandler sendActivityHandler, String filePath) {
        this.sendActivityHandler = sendActivityHandler;
        this.filePath = filePath;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {


        Looper.prepare();
        managerHandler = new Handler() {

            public void handleMessage(Message msg) {

                if (msg.what == RETRY) {
                    try {
                        socket = serverSocket.accept();
                        DateServerThread thread = new DateServerThread(sendActivityHandler, managerHandler, filePath, socket);
                        thread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            DateServerThread thread = new DateServerThread(sendActivityHandler, managerHandler, filePath, socket);
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
