package cn.edu.sdust.silence.itransfer.ui.fragment;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.activity.ReceiveFromComputerActivity;
import cn.edu.sdust.silence.itransfer.ui.TestRecyclerViewAdapter;
import cn.edu.sdust.silence.itransfer.ui.file.EventHandler;
import cn.edu.sdust.silence.itransfer.ui.file.FileManager;


/**
 * Created by florentchampigny on 24/04/15.
 */
public class RecyclerViewFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private FileManager mFileMag = new FileManager();
    private EventHandler mHandler;
    private boolean mReturnIntent = false;
    private TestRecyclerViewAdapter tAdapter;

    private final int KB = 1024;
    private final int MG = KB * KB;
    private final int GB = MG * KB;

    public boolean ismUseBackKey() {
        return mUseBackKey;
    }

    public void setmUseBackKey(boolean mUseBackKey) {
        this.mUseBackKey = mUseBackKey;
    }

    private boolean mUseBackKey = true;

    public static RecyclerViewFragment newInstance(String homedir) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("homeDir", homedir);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        String homeDir = getArguments().getString("homeDir");
        mFileMag = new FileManager();
        if (savedInstanceState != null)
            mHandler = new EventHandler(getActivity(), mFileMag, savedInstanceState.getString("location"), true);
        else {
            mHandler = new EventHandler(getActivity(), mFileMag, homeDir, false);
        }
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("location", mFileMag.getCurrentDir());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        tAdapter = new TestRecyclerViewAdapter();
        tAdapter.setOnItemClickListener(new TestRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                final String item = mHandler.getData(position);
                File file = new File(mFileMag.getCurrentDir() + "/" + item);
                String item_ext = null;
                try {
                    item_ext = item.substring(item.lastIndexOf("."), item.length());

                } catch (IndexOutOfBoundsException e) {
                    item_ext = "";
                }

                if (file.isDirectory()) {
                    if (file.canRead()) {
                        mHandler.stopThumbnailThread();
                        mHandler.updateDirectory(mFileMag.getNextDir(item, false));
                        if (!mUseBackKey)
                            mUseBackKey = true;

                    } else {
                        Toast.makeText(getActivity(), "权限不足，无法读取文件！",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (item_ext.equalsIgnoreCase(".mp3") ||
                        item_ext.equalsIgnoreCase(".m4a") ||
                        item_ext.equalsIgnoreCase(".mp4")) {
                    if (mReturnIntent) {
//                        returnIntentResults(file);
                    } else {
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(file), "audio/*");
                        startActivity(i);
                    }
                } else if (item_ext.equalsIgnoreCase(".jpeg") ||
                        item_ext.equalsIgnoreCase(".jpg") ||
                        item_ext.equalsIgnoreCase(".png") ||
                        item_ext.equalsIgnoreCase(".gif") ||
                        item_ext.equalsIgnoreCase(".tiff")) {
                    if (file.exists()) {
                        if (mReturnIntent) {
//                            returnIntentResults(file);
                        } else {
                            Intent picIntent = new Intent();
                            picIntent.setAction(Intent.ACTION_VIEW);
                            picIntent.setDataAndType(Uri.fromFile(file), "image/*");
                            startActivity(picIntent);
                        }
                    }
                } else if (item_ext.equalsIgnoreCase(".m4v") ||
                        item_ext.equalsIgnoreCase(".3gp") ||
                        item_ext.equalsIgnoreCase(".wmv") ||
                        item_ext.equalsIgnoreCase(".mp4") ||
                        item_ext.equalsIgnoreCase(".ogg") ||
                        item_ext.equalsIgnoreCase(".wav")) {

                    if (file.exists()) {
                        if (mReturnIntent) {
//                            returnIntentResults(file);

                        } else {
                            Intent movieIntent = new Intent();
                            movieIntent.setAction(Intent.ACTION_VIEW);
                            movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
                            startActivity(movieIntent);
                        }
                    }
                } else if (item_ext.equalsIgnoreCase(".zip")) {

                    if (mReturnIntent) {
//                        returnIntentResults(file);

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        AlertDialog alert;
                        CharSequence[] option = {"Extract here", "Extract to..."};

                        builder.setTitle("Extract");
                        builder.setItems(option, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        String dir = mFileMag.getCurrentDir();
                                        mHandler.unZipFile(item, dir + "/");
                                        break;

                                    case 1:
//                                        mDetailLabel.setText("Holding " + item +
//                                                " to extract");
                                        break;
                                }
                            }
                        });

                        alert = builder.create();
                        alert.show();
                    }
                } else if (item_ext.equalsIgnoreCase(".gzip") ||
                        item_ext.equalsIgnoreCase(".gz")) {

                    if (mReturnIntent) {
//                        returnIntentResults(file);

                    } else {
                        //TODO:
                    }
                } else if (item_ext.equalsIgnoreCase(".pdf")) {

                    if (file.exists()) {
                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            Intent pdfIntent = new Intent();
                            pdfIntent.setAction(Intent.ACTION_VIEW);
                            pdfIntent.setDataAndType(Uri.fromFile(file),
                                    "application/pdf");

                            try {
                                startActivity(pdfIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(), "Sorry, couldn't find a pdf viewer",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else if (item_ext.equalsIgnoreCase(".apk")) {

                    if (file.exists()) {
                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            Intent apkIntent = new Intent();
                            apkIntent.setAction(Intent.ACTION_VIEW);
                            apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            startActivity(apkIntent);
                        }
                    }
                } else if (item_ext.equalsIgnoreCase(".html")) {

                    if (file.exists()) {
                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            Intent htmlIntent = new Intent();
                            htmlIntent.setAction(Intent.ACTION_VIEW);
                            htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

                            try {
                                startActivity(htmlIntent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(), "Sorry, couldn't find a HTML viewer",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else if (item_ext.equalsIgnoreCase(".txt")) {

                    if (file.exists()) {
                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            Intent txtIntent = new Intent();
                            txtIntent.setAction(Intent.ACTION_VIEW);
                            txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

                            try {
                                startActivity(txtIntent);
                            } catch (ActivityNotFoundException e) {
                                txtIntent.setType("text/*");
                                startActivity(txtIntent);
                            }
                        }
                    }
                } else {
                    if (file.exists()) {
                        if (mReturnIntent) {
                            returnIntentResults(file);

                        } else {
                            Intent generic = new Intent();
                            generic.setAction(Intent.ACTION_VIEW);
                            generic.setDataAndType(Uri.fromFile(file), "text/plain");

                            try {
                                startActivity(generic);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(), "Sorry, couldn't find anything " +
                                                "to open " + file.getName(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                final String item = mHandler.getData(position);
                final String path = mFileMag.getCurrentDir() + "/" + item;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("请选择操作");
                builder.setItems(new String[]{"文件信息", "重命名", "删除文件"}, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO
                        if (which == 0) {
                            File file = new File(path);
                            String name = file.getName();
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                            String time = df.format(new Date(file.lastModified()));
                            double size = file.length();
                            String display_size = "";
                            if (size > GB)
                                display_size = String.format("%.2f Gb ", (double) size / GB);
                            else if (size < GB && size > MG)
                                display_size = String.format("%.2f Mb ", (double) size / MG);
                            else if (size < MG && size > KB)
                                display_size = String.format("%.2f Kb ", (double) size / KB);
                            else
                                display_size = String.format("%.2f bytes ", (double) size);

                            String msg = "文件名：" + name + "\n\n" + "大小：" + display_size + "\n\n" + "文件路径：" + path + "\n\n" + "修改时间：" + time;
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("文件详情 ");
                            builder.setMessage(msg);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else if (which == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("正在对 " + item + "重命名");
                            LayoutInflater inflater = LayoutInflater.from(getContext());
                            View view = inflater.inflate(R.layout.dialog_rename_file, null);
                            final AppCompatEditText newName = (AppCompatEditText) view.findViewById(R.id.newName);
                            builder.setView(view);
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (newName.getText().length() < 1)
                                        dialog.dismiss();

                                    if (mFileMag.renameTarget(path, newName.getText().toString()) == 0) {
                                        Toast.makeText(getContext(), item + " 重名名为 " + newName.getText().toString(),
                                                Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(getContext(), "重命名失败", Toast.LENGTH_LONG).show();

                                    dialog.dismiss();
                                    String temp = mFileMag.getCurrentDir();
                                    mHandler.updateDirectory(mFileMag.getNextDir(temp, true));
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        } else if (which == 2) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("警告 ");
                            builder.setMessage("确定要删除 " + item + "吗？此过程不可逆！");
                            builder.setCancelable(false);

                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mHandler.deleteFile(path);
                                }
                            });
                            builder.show();
                        }
                    }
                });
                builder.show();

            }
        });

        mAdapter = new RecyclerViewMaterialAdapter(tAdapter);
        mRecyclerView.setAdapter(mAdapter);
        mHandler.setRecyclerViewAdapter(tAdapter, mAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    private void returnIntentResults(File data) {
//        mReturnIntent = false;
//
//        Intent ret = new Intent();
//        ret.setData(Uri.fromFile(data));
//        setResult(RESULT_OK, ret);
//
//        finish();
    }

    public boolean isHomeDirectory() {
        String current = mFileMag.getCurrentDir();
        String homePath = Environment.getExternalStorageDirectory().getPath();
        return current.equals(homePath);
    }

    public boolean backPreviousDirectory() {

        mHandler.stopThumbnailThread();
        mHandler.backPreviousDirectory();
        return true;
    }

    public void reUpdateCurrentDir() {
        mHandler.updateDirectory(mFileMag.getCurrentDirContent());
    }

}
