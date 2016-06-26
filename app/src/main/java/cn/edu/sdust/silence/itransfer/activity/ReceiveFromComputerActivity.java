package cn.edu.sdust.silence.itransfer.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.handler.ReceiveFromComputerActivityHandler;
import cn.edu.sdust.silence.itransfer.ui.loading.RotateLoading;
import cn.edu.sdust.silence.itransfer.web.domain.FileLog;
import cn.edu.sdust.silence.itransfer.thread.DownLoadFileThread;
import cn.edu.sdust.silence.itransfer.thread.ListFileThread;

/**
 * create by shifeiqi
 */
public class ReceiveFromComputerActivity extends AppCompatActivity {

    private Button btn;
    private TextView text;
    private ReceiveFromComputerActivityHandler handler;
    private String fileCode, password;
    private List<FileLog> files;

    //loading相关view
    private RotateLoading loadingView;
    private TextView loadingText;
    private AlertDialog dialog;
    public static int TYPE_SCAN = 1;
    public static int TYPE_NO_SCAN = 2;
    private int TYPE;
    private AlertDialog listFileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_from_computer);

        initView();
        initData();
        listFile();
    }

    /**
     * 获取文件信息成功
     *
     * @param files
     */
    public void listSuccess(List<FileLog> files) {
        listFileDialog.dismiss();
        this.files = files;
        for (FileLog file : files) {
            text.setText("文件： " + file.getFilename());
        }
    }

    /**
     * 获取文件信息失败
     */
    public void listFail() {
        listFileDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFromComputerActivity.this);
        builder.setTitle("提示");
        builder.setMessage("获取失败，请重新获取");
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

    /**
     * 获取文件信息
     */
    private void listFile() {
        Log.i("abc", "fileCode: " + fileCode + ",password: " + password);
        ListFileThread thread = new ListFileThread(fileCode, password, handler);
        thread.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFromComputerActivity.this);
        builder.setTitle("提示");

        LayoutInflater inflater = LayoutInflater.from(ReceiveFromComputerActivity.this);
        View view = inflater.inflate(R.layout.dialog_download_file, null);
        loadingView = (RotateLoading) view.findViewById(R.id.loadingView);
        loadingView.setLoadingColor(Color.GRAY);
        loadingText = (TextView) view.findViewById(R.id.loadingText);
        loadingText.setText("获取信息,请稍等...");
        loadingView.start();

        builder.setView(view);
        builder.setPositiveButton("取消获取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        listFileDialog = builder.show();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        handler = new ReceiveFromComputerActivityHandler(ReceiveFromComputerActivity.this);

        Bundle bundle = getIntent().getExtras();
        TYPE = bundle.getInt("flag");
        if (TYPE == TYPE_SCAN) {
            String result = bundle.getString("result");
            fileCode = result.substring(result.indexOf("=") + 1, result.indexOf("&"));
            password = result.substring(result.indexOf("=", result.indexOf("&") + 1) + 1, result.indexOf("&", result.indexOf("&") + 1));
        } else if (TYPE == TYPE_NO_SCAN) {
            fileCode = bundle.getString("fileCode");
            password = bundle.getString("password");
        }


    }

    /**
     * 初始化view
     */
    public void initView() {
        btn = (Button) findViewById(R.id.btn);
        text = (TextView) findViewById(R.id.text);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFiles();

                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFromComputerActivity.this);
                builder.setTitle("提示");

                LayoutInflater inflater = LayoutInflater.from(ReceiveFromComputerActivity.this);
                View view = inflater.inflate(R.layout.dialog_download_file, null);
                loadingView = (RotateLoading) view.findViewById(R.id.loadingView);
                loadingView.setLoadingColor(Color.GRAY);
                loadingText = (TextView) view.findViewById(R.id.loadingText);
                loadingText.setText("正在下载,请稍等...");
                loadingView.start();

                builder.setView(view);
                builder.setPositiveButton("取消下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                dialog = builder.show();

            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveFromComputerActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定要退出接收文件吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * 下载文件
     */
    private void downloadFiles() {
        for (int i = 0; i < files.size(); i++) {
            FileLog file = files.get(i);
            DownLoadFileThread thread = new DownLoadFileThread(file.getFid() + "",file.getFilecode() + "",
                    file.getStoreName(), Environment.getExternalStorageDirectory() + "/iTransfer/files/" + file.getFilename(),
                    handler);
            thread.start();
        }
    }

    /**
     * 下载文件成功
     */
    public void downloadSuccess() {
        dialog.dismiss();
        AlertDialog.Builder builderTmp = new AlertDialog.Builder(this);
        builderTmp.setCancelable(false);
        builderTmp.setTitle("提示");
        builderTmp.setMessage("下载完成");
        builderTmp.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builderTmp.show();
    }

    /**
     * 下载文件失败
     */
    public void downloadFail() {
        dialog.dismiss();
        AlertDialog.Builder builderTmp = new AlertDialog.Builder(this);
        builderTmp.setTitle("提示");
        builderTmp.setMessage("下载失败");
        builderTmp.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builderTmp.show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确定要退出接收文件吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        return super.onKeyDown(keyCode, event);
    }
}
