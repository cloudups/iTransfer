package cn.edu.sdust.silence.itransfer.web.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 宇强 on 2016/4/30 0030.
 */
public class FileLog implements Serializable{

    /**
     * 文件唯一id
     */
    private int fid;
    /**
     * 用户文件名
     */
    private String filename;
    /**
     * 服务器文件储存名
     */
    private String storeName;
    /**
     * 该文件md5
     */
    private String md5;
    /**
     * 文件后缀名
     */
    private String extension;
    /**
     * 文件上传时间
     */
    private Date time;
    /**
     * 文件唯一查找识别id
     */
    private int filecode;
    /**
     * 文件安全验证密码
     */
    private String password;

    @Override
    public String toString() {
        return "FileLog{" +
                "fid=" + fid +
                ", filename='" + filename + '\'' +
                ", storeName='" + storeName + '\'' +
                ", md5='" + md5 + '\'' +
                ", extension='" + extension + '\'' +
                ", time=" + time +
                ", filecode=" + filecode +
                ", password='" + password + '\'' +
                '}';
    }

    public FileLog() {
    }

    public void init(String filename, String storeName, String md5, String extension, Date time, int filecode, String password) {
        this.filename = filename;
        this.storeName = storeName;
        this.md5 = md5;
        this.extension = extension;
        this.time = time;
        this.filecode = filecode;
        this.password = password;
    }

    public int getFilecode() {
        return filecode;
    }

    public void setFilecode(int filecode) {
        this.filecode = filecode;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
