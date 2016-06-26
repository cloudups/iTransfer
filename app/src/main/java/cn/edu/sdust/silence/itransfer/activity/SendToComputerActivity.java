package cn.edu.sdust.silence.itransfer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.handler.SendToComputerActivityHandler;
import cn.edu.sdust.silence.itransfer.ui.loading.RotateLoading;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;
import cn.edu.sdust.silence.itransfer.thread.UploadFileThread;

/**
 * create by shifeiqi
 */
public class SendToComputerActivity extends AppCompatActivity {

    private SendToComputerActivityHandler handler;
    public static int TYPE_SUCCESS = 1;
    public static int TYPE_FAIL = 2;
    public static int TYPE_SCAN = 3;
    private List<FileLog> fileLogs;
    private AlertDialog dialog;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_computer);
        initView();
        initData();
        uploadFile();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        handler = new SendToComputerActivityHandler(SendToComputerActivity.this);
    }

    /**
     * 上传文件
     */
    private void uploadFile() {
        String path = getIntent().getStringExtra("path");
        List<File> files = new ArrayList<File>();
        files.add(new File(path));
        String password = (int) (Math.random() * 1000 + 1000) + "";
        UploadFileThread thread = new UploadFileThread(password, files, handler);
        thread.start();
    }

    /**
     * 初始化view
     */
    private void initView() {
        image = (ImageView) findViewById(R.id.image);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.4f, Animation.RELATIVE_TO_PARENT,
                -1.0f);
        animation.setDuration(3500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        image.startAnimation(animation);

        AlertDialog.Builder builder = new AlertDialog.Builder(SendToComputerActivity.this);
        builder.setTitle("提示");

        LayoutInflater inflater = LayoutInflater.from(SendToComputerActivity.this);
        View view = inflater.inflate(R.layout.dialog_download_file, null);
        RotateLoading loadingView = (RotateLoading) view.findViewById(R.id.loadingView);
        loadingView.setLoadingColor(Color.GRAY);
        TextView loadingText = (TextView) view.findViewById(R.id.loadingText);
        loadingText.setText("正在上传,请稍等...");
        loadingView.start();

        builder.setView(view);
        builder.setPositiveButton("取消上传", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        dialog = builder.show();

    }

    public void uploadSuccess(List<FileLog> fileLogs) {
        dialog.dismiss();
        this.fileLogs = fileLogs;
        Intent intent = new Intent(SendToComputerActivity.this, CaptureActivity.class);
        intent.putExtra("type", CaptureActivity.TYPE_INTENT_SEND);
        intent.putExtra("fileLogs", (Serializable) fileLogs);
        startActivity(intent);
        finish();
    }

    public void uploadFail() {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setCancelable(false);
        builder.setMessage("上传失败,请重新上传文件");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();
    }

    public void scanSuccess(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(result);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 1 && resultCode == 1) {
//            String result = data.getStringExtra("result");
//            String fileName = result.substring(result.indexOf("=", result.indexOf("&")) + 1);
//            Log.i("abc", "fileName: " + fileName);
//
//            for (int i = 0; i < fileLogs.size(); i++) {
//                FileLog file = fileLogs.get(i);
//                ScanThread thread = new ScanThread("" + file.getFilecode(), file.getPassword(), fileName, handler);
//                thread.start();
//            }
//        }
    }
}
