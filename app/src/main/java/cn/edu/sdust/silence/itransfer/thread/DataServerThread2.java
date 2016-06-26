package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import cn.edu.sdust.silence.itransfer.activity.SendActivity;
import cn.edu.sdust.silence.itransfer.handler.SendActivityHandler;

/**
 * 发送文件线程
 *
 * Created by feiqishi on 2016/5/5.
 */
public class DataServerThread2 extends Thread {


    private String ip;
    private long length;
    private String fileName;
    private String filePath;

    private Handler managerHandler;
    private SendActivityHandler sendActivityHandler;

    public DataServerThread2(SendActivityHandler sendActivityHandler, Handler managerHandler, String ip, String filePath) {
        this.sendActivityHandler = sendActivityHandler;
        this.ip = ip;
        this.filePath = filePath;
        length = 0;

        this.managerHandler = managerHandler;
    }

    @Override
    public void run() {

        Socket socket = new Socket();

        try {
            socket.connect((new InetSocketAddress(ip, 8888)),
                    5000);

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            File file = new File(filePath);

            fileName = file.getName();
            os.write(fileName.getBytes());

            String serverInfo = servInfoBack(is);
            if (serverInfo.equals("FileLengthSendNow")) {
                length = file.length();
                os.write(("" + length).getBytes());
            }

            String serverInfo2 = servInfoBack(is);
            if (serverInfo2.equals("FileSendNow")) {
                FileInputStream inputStream = new FileInputStream(file);
                copyFile(inputStream, os);
                inputStream.close();
            }

            is.close();
            os.close();
            socket.close();
            sendFinishMessage();
        } catch (Exception e) {
            sendErrorMessage();
            e.printStackTrace();
        }
        super.run();
    }


    public String servInfoBack(InputStream is) throws Exception {
        byte[] bufIs = new byte[1024];
        int lenIn = is.read(bufIs);
        String info = new String(bufIs, 0, lenIn);
        return info;
    }

    public boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long sendLength = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                sendLength += len;
                sendProgress((int) (sendLength * 100 / length));
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void sendErrorMessage() {
        Message msg = new Message();
        msg.what = ServerManager2.RETRY;
        managerHandler.sendMessage(msg);
    }

    private void sendFinishMessage() {
        Message msg = new Message();
        msg.what = ServerManager2.FINISH;
        managerHandler.sendMessage(msg);
    }

    private void sendProgress(int progress) {
        Message msg = new Message();
        msg.what = SendActivity.PROGRESS;
        msg.arg1 = progress;
        sendActivityHandler.sendMessage(msg);
    }
}
