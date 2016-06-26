package cn.edu.sdust.silence.itransfer.thread;

import android.os.Message;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.sdust.silence.itransfer.activity.SendToComputerActivity;
import cn.edu.sdust.silence.itransfer.handler.SendToComputerActivityHandler;
import cn.edu.sdust.silence.itransfer.web.ITransferClient;
import cn.edu.sdust.silence.itransfer.web.ITransferClientImpl;
import cn.edu.sdust.silence.itransfer.web.ResponseParser;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;

/**
 * Created by shifeiqi on 16-6-8.
 *
 */
public class UploadFileThread extends Thread {


    private String password;
    private List<File> files;
    private ITransferClient client = null;
    private SendToComputerActivityHandler handler;
    private List<FileLog> fileLogs;

    public UploadFileThread(String password, List<File> files, SendToComputerActivityHandler handler) {
        this.password = password;
        this.files = files;
        this.handler = handler;
        client = new ITransferClientImpl();
    }

    @Override
    public void run() {

        try {
            uploadFiles(password, files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException
     * @throws ClientProtocolException
     */
    public List<FileLog> uploadFiles(String password, List<File> files) throws ClientProtocolException, IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("password", password);// 临时验证密码

        ResponseParser parser = client.sendFileForm("upload", files, params);
        ResponseParser.ResultType type = parser.parse();
        if (type == ResponseParser.ResultType.Error) {
            System.out.println("错误信息：" + parser.getResult().getMessage());
            Message msg = new Message();
            msg.what = SendToComputerActivity.TYPE_FAIL;
            handler.sendMessage(msg);
        } else if (type == ResponseParser.ResultType.FileLog) {
            System.out.println("已上传的文件列表：" + parser.getResult().getFileLogs());
            for (FileLog fileLog : parser.getResult().getFileLogs()) {
                System.out.println(fileLog);
            }
            fileLogs = parser.getResult().getFileLogs();

            Message msg = new Message();
            msg.what = SendToComputerActivity.TYPE_SUCCESS;
            msg.obj = fileLogs;
            handler.sendMessage(msg);
            return fileLogs;
        }
        return null;
    }
}
