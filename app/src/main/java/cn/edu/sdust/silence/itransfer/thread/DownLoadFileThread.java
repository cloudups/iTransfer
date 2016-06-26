package cn.edu.sdust.silence.itransfer.thread;

import android.os.Message;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

import cn.edu.sdust.silence.itransfer.handler.ReceiveFromComputerActivityHandler;
import cn.edu.sdust.silence.itransfer.web.ITransferClient;
import cn.edu.sdust.silence.itransfer.web.ITransferClientImpl;
import cn.edu.sdust.silence.itransfer.web.ResponseParser;

/**
 * Created by shifeiqi on 16-6-8.
 */
public class DownLoadFileThread extends Thread {

    private String fileName, storeName, path,fid;
    private ITransferClient client = null;
    private ReceiveFromComputerActivityHandler handler;

    public DownLoadFileThread(String fid,String fileName, String storeName, String path, ReceiveFromComputerActivityHandler handler) {
        this.fid = fid;
        this.fileName = fileName;
        this.storeName = storeName;
        this.path = path;
        this.handler = handler;
        client = new ITransferClientImpl();
    }

    @Override
    public void run() {
        try {
            downloadFiles(fid,fileName, storeName, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void downloadFiles(String fid,String fileName, String storeName, String path) throws IOException {
        // 提供要下载文件的filename，storeName和下载文件保存路径即可
        ResponseParser parser = client.downloadFile(fid,fileName, storeName, path);
        ResponseParser.ResultType type = parser.parse();
        if (type == ResponseParser.ResultType.Error) {
            Message msg = new Message();
            msg.what = ReceiveFromComputerActivityHandler.TYPE_DOWNLOAD_FAIL;
            handler.sendMessage(msg);
//            System.out.println("错误信息：" + parser.getResult().getMessage());
        } else if (type == ResponseParser.ResultType.Download) {
            Message msg = new Message();
            msg.what = ReceiveFromComputerActivityHandler.TYPE_DOWNLOAD_SUCCESS;
            handler.sendMessage(msg);
            System.out.println("文件下载成功！！！");
        }
    }
}

