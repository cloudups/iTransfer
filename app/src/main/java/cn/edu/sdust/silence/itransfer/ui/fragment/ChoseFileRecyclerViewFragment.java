package cn.edu.sdust.silence.itransfer.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import java.io.File;
import java.util.ArrayList;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.activity.ChoseFileActivity;
import cn.edu.sdust.silence.itransfer.ui.TestRecyclerViewAdapter;
import cn.edu.sdust.silence.itransfer.ui.file.EventHandler;
import cn.edu.sdust.silence.itransfer.ui.file.FileManager;
import cn.edu.sdust.silence.itransfer.activity.SendActivity;


/**
 * Created by florentchampigny on 24/04/15.
 */
public class ChoseFileRecyclerViewFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private FileManager mFileMag;
    private EventHandler mHandler;

    private boolean mUseBackKey = true;
    private ChoseFileActivity context;

    public boolean ismUseBackKey() {
        return mUseBackKey;
    }

    public void setmUseBackKey(boolean mUseBackKey) {
        this.mUseBackKey = mUseBackKey;
    }


    public static ChoseFileRecyclerViewFragment newInstance() {
        return new ChoseFileRecyclerViewFragment();
    }

    public void setContext(ChoseFileActivity context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mFileMag = new FileManager();
        if (savedInstanceState != null)
            mHandler = new EventHandler(getActivity(), mFileMag, savedInstanceState.getString("location"), true);
        else
            mHandler = new EventHandler(getActivity(), mFileMag, Environment.getExternalStorageDirectory().toString(), false);

        return inflater.inflate(R.layout.fragment_chosefile_recyclerview, container, false);
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

        TestRecyclerViewAdapter tAdapter = new TestRecyclerViewAdapter();
        tAdapter.setOnItemClickListener(new TestRecyclerViewAdapter.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(View view, int position) {
                                                final String item = mHandler.getData(position);
                                                String path = mFileMag.getCurrentDir() + "/" + item;
                                                File file = new File(path);
                                                if (file.isDirectory()) {
                                                    if (file.canRead()) {
                                                        mHandler.stopThumbnailThread();
                                                        mHandler.updateDirectory(mFileMag.getNextDir(item, false));
//                                                        Toast.makeText(getContext(), "click  " + item + ", position " + position, Toast.LENGTH_LONG).show();

                                                        if (!mUseBackKey)
                                                            mUseBackKey = true;

                                                    } else {
                                                        Toast.makeText(getActivity(), "Can't read folder due to permissions",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Intent intent = new Intent(context, SendActivity.class);
                                                    intent.putExtra("path", path);
                                                    startActivity(intent);
                                                    context.finish();
                                                }
                                            }

                                            @Override
                                            public void onItemLongClick(View view, int position) {
                                            }
                                        }

        );

        mAdapter = new RecyclerViewMaterialAdapter(tAdapter);

        mRecyclerView.setAdapter(mAdapter);
        mHandler.setRecyclerViewAdapter(tAdapter, mAdapter);

        MaterialViewPagerHelper.registerRecyclerView(
                getActivity(), mRecyclerView,
                null);
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
}
