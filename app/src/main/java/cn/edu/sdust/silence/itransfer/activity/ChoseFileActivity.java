package cn.edu.sdust.silence.itransfer.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.ui.fragment.ChoseFileRecyclerViewFragment;

/**
 *
 * create by shifeiqi
 */
public class ChoseFileActivity extends FragmentActivity {

    private ChoseFileRecyclerViewFragment recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_file);
        initView();
    }

    private void initView() {
        recyclerView = ChoseFileRecyclerViewFragment.newInstance();
        recyclerView.setContext(this);
        setDefaultFragment();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChoseFileActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定要取消选择发送文件吗？");
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

    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.frame, recyclerView);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean key = recyclerView.ismUseBackKey();
        boolean isHomePath = recyclerView.isHomeDirectory();
        if (keyCode == KeyEvent.KEYCODE_BACK && key && !isHomePath) {
            recyclerView.backPreviousDirectory();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && key && isHomePath) {
            finish();
            return false;
        }
        return false;
    }
}
