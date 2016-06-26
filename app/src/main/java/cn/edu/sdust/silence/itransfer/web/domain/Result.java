package cn.edu.sdust.silence.itransfer.web.domain;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by 宇强 on 2016/5/24 0024.
 */
public class Result {
    private String type;
    private List<FileLog> fileLogs;
    private String message;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileLog> getFileLogs() {
        return fileLogs;
    }

    public void setFileLogs(List<FileLog> fileLogs) {
        this.fileLogs = fileLogs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
