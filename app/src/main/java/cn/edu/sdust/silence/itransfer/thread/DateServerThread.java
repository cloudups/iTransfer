package cn.edu.sdust.silence.itransfer.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cn.edu.sdust.silence.itransfer.activity.SendActivity;
import cn.edu.sdust.silence.itransfer.handler.SendActivityHandler;

/**
 * 文件发送线程
 *
 * Created by feiqishi on 2016/5/5.
 */
public class DateServerThread extends Thread {


    private String fileName;
    private long length;
    private String filePath;
    private Socket socket;

    private Handler managerHandler;
    private SendActivityHandler sendActivityHandler;

    public DateServerThread(SendActivityHandler sendActivityHandler, Handler managerHandler, String filePath, Socket socket) {
        this.sendActivityHandler = sendActivityHandler;
        this.filePath = filePath;
        this.socket = socket;
        this.managerHandler = managerHandler;
        Log.i("xyz", "dataServerThread start");
    }

    @Override
    public void run() {
        try {
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
            sendFinishMessage();
        } catch (IOException e) {
            sendErrorMessage();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        msg.what = ServerManager.RETRY;
        managerHandler.sendMessage(msg);
    }

    private void sendFinishMessage() {
        Message msg = new Message();
        msg.what = ServerManager.FINISH;
        managerHandler.sendMessage(msg);
    }

    private void sendProgress(int progress) {
        Message msg = new Message();
        msg.what = SendActivity.PROGRESS;
        msg.arg1 = progress;
        sendActivityHandler.sendMessage(msg);
    }
}
