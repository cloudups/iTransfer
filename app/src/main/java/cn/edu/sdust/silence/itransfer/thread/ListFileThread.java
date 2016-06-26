package cn.edu.sdust.silence.itransfer.thread;

import android.os.Message;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.sdust.silence.itransfer.handler.ReceiveFromComputerActivityHandler;
import cn.edu.sdust.silence.itransfer.web.ITransferClient;
import cn.edu.sdust.silence.itransfer.web.ITransferClientImpl;
import cn.edu.sdust.silence.itransfer.web.ResponseParser;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;

/**
 * Created by shifeiqi on 16-6-8.
 */
public class ListFileThread extends Thread {

    private String fileCode, password;
    private ITransferClient client = null;
    private ReceiveFromComputerActivityHandler handler;
    private List<FileLog> files;

    public ListFileThread(String fileCode, String password, ReceiveFromComputerActivityHandler handler) {
        this.fileCode = fileCode;
        this.password = password;
        this.handler = handler;
        client = new ITransferClientImpl();
    }

    @Override
    public void run() {
        try {
            listFiles(fileCode, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException
     */
    public void listFiles(String fileCode, String password) throws IOException {
        // 提供文件id和临时密码即可查看文件
        Map<String, String> params = new HashMap<String, String>();
        params.put("filecode", fileCode);
        params.put("password", password);
        ResponseParser parser = client.sendForm("query", params);
        ResponseParser.ResultType type = parser.parse();
        if (type == ResponseParser.ResultType.Error) {
            System.out.println("错误信息：" + parser.getResult().getMessage());
            Message message = new Message();
            message.what = ReceiveFromComputerActivityHandler.TYPE_LIST_FAIL;
            handler.sendMessage(message);
        } else if (type == ResponseParser.ResultType.FileLog) {
            files = parser.getResult().getFileLogs();
            Message message = new Message();
            message.what = ReceiveFromComputerActivityHandler.TYPE_LIST_SUCCESS;
            message.obj = files;
            handler.sendMessage(message);
        }
    }
}
