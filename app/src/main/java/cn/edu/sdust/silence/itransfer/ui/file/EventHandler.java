package cn.edu.sdust.silence.itransfer.ui.file;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import java.util.ArrayList;

import cn.edu.sdust.silence.itransfer.ui.TestRecyclerViewAdapter;


public class EventHandler {
    /*
     * Unique types to control which file operation gets
     * performed in the background
     */
    private static final int SEARCH_TYPE = 0x00;
    private static final int COPY_TYPE = 0x01;
    private static final int UNZIP_TYPE = 0x02;
    private static final int UNZIPTO_TYPE = 0x03;
    private static final int ZIP_TYPE = 0x04;
    private static final int DELETE_TYPE = 0x05;

    private final Context mContext;
    private final FileManager mFileMang;
    private ThumbnailCreator mThumbnail;
    //    private TestRecyclerViewAdapter mDelegate;
    private RecyclerViewMaterialAdapter mDelegate;
    private TestRecyclerViewAdapter tAdapter;

    private boolean multi_select_flag = false;
    private boolean delete_after_copy = false;
    private boolean thumbnail_flag = true;
    private int mColor = Color.WHITE;

    //the list used to feed info into the array adapter and when multi-select is on
    private ArrayList<String> mDataSource, mMultiSelectData;
    //    private ArrayList<ViewHolder> mDataSource;
    private TextView mPathLabel;
    private TextView mInfoLabel;


    /**
     * Creates an EventHandler object. This object is used to communicate
     * most work from the Main activity to the FileManager class.
     *
     * @param context The context of the main activity e.g  Main
     * @param manager The FileManager object that was instantiated from Main
     */
    public EventHandler(Context context, final FileManager manager, String homeDir) {
        mContext = context;
        mFileMang = manager;

        mDataSource = new ArrayList<String>(mFileMang.setHomeDir(homeDir));
    }

    /**
     * This constructor is called if the user has changed the screen orientation
     * and does not want the directory to be reset to home.
     *
     * @param context  The context of the main activity e.g  Main
     * @param manager  The FileManager object that was instantiated from Main
     * @param location The first directory to display to the user
     */
    public EventHandler(Context context, final FileManager manager, String location, boolean savedInstanceState) {
        mContext = context;
        mFileMang = manager;
        if (savedInstanceState)
            mDataSource = new ArrayList<String>(mFileMang.getNextDir(location, true));
        else
            mDataSource = new ArrayList<String>(mFileMang.setHomeDir(location));
    }

    /**
     * This method is called from the Main activity and this has the same
     * reference to the same object so when changes are made here or there
     * they will display in the same way.
     *
     * @param adapter The TableRow object
     */
//    public void setRecyclerViewAdapter(TestRecyclerViewAdapter adapter) {
//        mDelegate = adapter;
////        mDelegate.upAllData(mFileMang.getCurrentDir(), mDataSource);
//    }
    public void setRecyclerViewAdapter(RecyclerView.Adapter tAdapter, RecyclerView.Adapter adapter) {
        this.tAdapter = (TestRecyclerViewAdapter) tAdapter;
        mDelegate = (RecyclerViewMaterialAdapter) adapter;
        this.tAdapter.upAllData(mFileMang.getCurrentDir(), mDataSource);
        mDelegate.mvp_notifyDataSetChanged();
    }

    /**
     * This method is called from the Main activity and is passed
     * the TextView that should be updated as the directory changes
     * so the user knows which folder they are in.
     *
     * @param path  The label to update as the directory changes
     * @param label the label to update information
     */
    public void setUpdateLabels(TextView path, TextView label) {
        mPathLabel = path;
        mInfoLabel = label;
    }

    /**
     * @param color
     */
    public void setTextColor(int color) {
        mColor = color;
    }


    /**
     * This will extract a zip file to the same directory.
     *
     * @param file the zip file name
     * @param path the path were the zip file will be extracted (the current directory)
     */
    public void unZipFile(String file, String path) {
        new BackgroundWork(UNZIP_TYPE).execute(file, path);
    }


    /**
     * this will stop our background thread that creates thumbnail icons
     * if the thread is running. this should be stopped when ever
     * we leave the folder the image files are in.
     */
    public void stopThumbnailThread() {
        if (mThumbnail != null) {
            mThumbnail.setCancelThumbnails(true);
            mThumbnail = null;
        }
    }

    /**
     * will return the data in the ArrayList that holds the dir contents.
     *
     * @param position the indext of the arraylist holding the dir content
     * @return the data in the arraylist at position (position)
     */
    public String getData(int position) {

        if (position > mDataSource.size() - 1 || position < 0)
            return null;

        return mDataSource.get(position);
    }

    /**
     * called to update the file contents as the user navigates there
     * phones file system.
     *
     * @param content an ArrayList of the file/folders in the current directory.
     */
    public void updateDirectory(ArrayList<String> content) {

        if (!mDataSource.isEmpty())
            mDataSource.clear();

        for (String data : content)
            mDataSource.add(data);

        tAdapter.upAllData(mFileMang.getCurrentDir(), mDataSource);
        mDelegate.mvp_notifyDataSetChanged();
    }

    public void setHomeDir(String homeDir) {
        if (!mDataSource.isEmpty())
            mDataSource.clear();
        mDataSource.addAll(mFileMang.setHomeDir(homeDir));
        tAdapter.upAllData(mFileMang.getCurrentDir(), mDataSource);
        mDelegate.mvp_notifyDataSetChanged();
    }

    public void backPreviousDirectory() {

        if (!mDataSource.isEmpty())
            mDataSource.clear();

        mDataSource.addAll(mFileMang.getPreviousDir());
        tAdapter.upAllData(mFileMang.getCurrentDir(), mDataSource);
        mDelegate.mvp_notifyDataSetChanged();
    }

    /**
     * Will delete the file name that is passed on a background
     * thread.
     *
     * @param name
     */
    public void deleteFile(String name) {
        new BackgroundWork(DELETE_TYPE).execute(name);
    }

    private static class ViewHolder {
        TextView topView;
        TextView bottomView;
        ImageView icon;
        ImageView mSelect;    //multi-select check mark icon
    }

    /**
     * A private inner class of EventHandler used to perform time extensive
     * operations. So the user does not think the the application has hung,
     * operations such as copy/past, search, unzip and zip will all be performed
     * in the background. This class extends AsyncTask in order to give the user
     * a progress dialog to show that the app is working properly.
     * <p/>
     * (note): this class will eventually be changed from using AsyncTask to using
     * Handlers and messages to perform background operations.
     *
     * @author Joe Berria
     */
    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
        private String file_name;
        private ProgressDialog pr_dialog;
        private int type;
        private int copy_rtn;

        private BackgroundWork(int type) {
            this.type = type;
        }

        /**
         * This is done on the EDT thread. this is called before
         * doInBackground is called
         */
        @Override
        protected void onPreExecute() {

            switch (type) {
                case SEARCH_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Searching",
                            "Searching current file system...",
                            true, true);
                    break;

                case COPY_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Copying",
                            "Copying file...",
                            true, false);
                    break;

                case UNZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case UNZIPTO_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Unzipping",
                            "Unpacking zip file please wait...",
                            true, false);
                    break;

                case ZIP_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Zipping",
                            "Zipping folder...",
                            true, false);
                    break;

                case DELETE_TYPE:
                    pr_dialog = ProgressDialog.show(mContext, "Deleting",
                            "Deleting files...",
                            true, false);
                    break;
            }
        }

        /**
         * background thread here
         */
        @Override
        protected ArrayList<String> doInBackground(String... params) {

            switch (type) {
                case SEARCH_TYPE:
                    file_name = params[0];
                    ArrayList<String> found = mFileMang.searchInDirectory(mFileMang.getCurrentDir(),
                            file_name);
                    return found;

                case COPY_TYPE:
                    int len = params.length;

                    if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                        for (int i = 1; i < len; i++) {
                            copy_rtn = mFileMang.copyToDirectory(params[i], params[0]);

                            if (delete_after_copy)
                                mFileMang.deleteTarget(params[i]);
                        }
                    } else {
                        copy_rtn = mFileMang.copyToDirectory(params[0], params[1]);

                        if (delete_after_copy)
                            mFileMang.deleteTarget(params[0]);
                    }

                    delete_after_copy = false;
                    return null;

                case UNZIP_TYPE:
                    mFileMang.extractZipFiles(params[0], params[1]);
                    return null;

                case UNZIPTO_TYPE:
                    mFileMang.extractZipFilesFromDir(params[0], params[1], params[2]);
                    return null;

                case ZIP_TYPE:
                    mFileMang.createZipFile(params[0]);
                    return null;

                case DELETE_TYPE:
                    int size = params.length;

                    for (int i = 0; i < size; i++)
                        mFileMang.deleteTarget(params[i]);

                    return null;
            }
            return null;
        }

        /**
         * This is called when the background thread is finished. Like onPreExecute, anything
         * here will be done on the EDT thread.
         */
        @Override
        protected void onPostExecute(final ArrayList<String> file) {
            final CharSequence[] names;
            int len = file != null ? file.size() : 0;

            switch (type) {
                case SEARCH_TYPE:
                    if (len == 0) {
                        Toast.makeText(mContext, "Couldn't find " + file_name,
                                Toast.LENGTH_SHORT).show();

                    } else {
                        names = new CharSequence[len];

                        for (int i = 0; i < len; i++) {
                            String entry = file.get(i);
                            names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Found " + len + " file(s)");
                        builder.setItems(names, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int position) {
                                String path = file.get(position);
                                updateDirectory(mFileMang.getNextDir(path.
                                        substring(0, path.lastIndexOf("/")), true));
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    pr_dialog.dismiss();
                    break;

                case COPY_TYPE:
                    if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                        multi_select_flag = false;
                        mMultiSelectData.clear();
                    }

                    if (copy_rtn == 0)
                        Toast.makeText(mContext, "File successfully copied and pasted",
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();

                    pr_dialog.dismiss();
                    mInfoLabel.setText("");
                    break;

                case UNZIP_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case UNZIPTO_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case ZIP_TYPE:
                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
                    break;

                case DELETE_TYPE:
                    if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
                        mMultiSelectData.clear();
                        multi_select_flag = false;
                    }

                    updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
                    pr_dialog.dismiss();
//                    mInfoLabel.setText("");
                    break;
            }
        }
    }
}
