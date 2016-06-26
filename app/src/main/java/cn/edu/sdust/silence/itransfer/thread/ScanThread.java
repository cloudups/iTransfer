package cn.edu.sdust.silence.itransfer.thread;

import android.os.Message;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.edu.sdust.silence.itransfer.qrcode.utils.CaptureActivityHandler;
import cn.edu.sdust.silence.itransfer.web.ITransferClient;
import cn.edu.sdust.silence.itransfer.web.ITransferClientImpl;
import cn.edu.sdust.silence.itransfer.web.ResponseParser;

/**
 * Created by shifeiqi on 16-6-8.
 */
public class ScanThread extends Thread {


    private ITransferClient client = null;
    private String fileCode, password, fileName;
    private CaptureActivityHandler handler;

    public ScanThread(String fileCode, String password, String fileName, CaptureActivityHandler handler) {
        this.fileCode = fileCode;
        this.password = password;
        this.fileName = fileName;
        this.handler = handler;
        client = new ITransferClientImpl();
    }

    @Override
    public void run() {
        try {
            scan(fileCode, password, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描二维码
     *
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void scan(String fileCode, String password, String fileName) throws IOException {
        // 提供文件id和临时密码即可查看文件
        Map<String, String> params = new HashMap<String, String>();
        //手机本身有的
        params.put("filecode", fileCode);
        params.put("password", password);

        //二维码扫描得到的
        params.put("filename", fileName);

        ResponseParser parser = client.sendForm("scan", params);
        System.out.println(parser.getObj());

        Message msg = new Message();
        msg.what = CaptureActivityHandler.SCAN_FINISH;
        msg.obj = parser.getObj();
        handler.sendMessage(msg);
    }
}
