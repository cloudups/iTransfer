package cn.edu.sdust.silence.itransfer.ui;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.ui.file.ThumbnailCreator;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class TestRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int KB = 1024;
    private final int MG = KB * KB;
    private final int GB = MG * KB;
    private String display_size;

    private boolean thumbnail_flag = true;
    private ThumbnailCreator mThumbnail;

    List<Object> contents;
    private ArrayList<String> mDataSource;
    private String path;

    static final int TYPE_TAIL = 0;
    static final int TYPE_CELL = 1;

    private OnItemClickListener onItemClickListener;


    public TestRecyclerViewAdapter(ArrayList<String> dataSource) {
        this.mDataSource = dataSource;
    }

    public TestRecyclerViewAdapter() {
        mDataSource = new ArrayList<String>();
    }

    @Override
    public int getItemViewType(int position) {
        int last = mDataSource.size() - 1;
        if (position == last)
            return TYPE_TAIL;
        else
            return TYPE_CELL;
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        switch (viewType) {
            case TYPE_TAIL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_big, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_small, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }
        }
        return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (position == mDataSource.size() - 1) {
            return;
        }

        int num_items = 0;
        File file = new File(path + "/" + mDataSource.get(position));
        String[] list = file.list();
        if (list != null)
            num_items = list.length;

        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.row_image);
        TextView topView = (TextView) holder.itemView.findViewById(R.id.top_view);
        TextView bottomView = (TextView) holder.itemView.findViewById(R.id.bottom_view);

        if (mThumbnail == null)
            mThumbnail = new ThumbnailCreator(52, 52);

        if (file != null && file.isFile()) {
            String ext = file.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

            if (sub_ext.equalsIgnoreCase("pdf")) {
                icon.setImageResource(R.drawable.pdf_icon);

            } else if (sub_ext.equalsIgnoreCase("mp3") ||
                    sub_ext.equalsIgnoreCase("wma") ||
                    sub_ext.equalsIgnoreCase("m4a") ||
                    sub_ext.equalsIgnoreCase("m4p")) {

                icon.setImageResource(R.drawable.music_icon);

            } else if (sub_ext.equalsIgnoreCase("png") ||
                    sub_ext.equalsIgnoreCase("jpg") ||
                    sub_ext.equalsIgnoreCase("jpeg") ||
                    sub_ext.equalsIgnoreCase("gif") ||
                    sub_ext.equalsIgnoreCase("tiff")) {

//                if (thumbnail_flag && file.length() != 0) {
//                    String filePath = file.getPath();
//                    Bitmap thumb = mThumbnail.isBitmapCached(filePath);
//                    if (thumb == null) {
//                        final Handler handle = new Handler(new Handler.Callback() {
//                            public boolean handleMessage(Message msg) {
//                                notifyDataSetChanged();
//                                return true;
//                            }
//                        });
//                        mThumbnail.createNewThumbnail(mDataSource, path, handle);
//                        if (!mThumbnail.isAlive())
//                            mThumbnail.stop();
//                            mThumbnail.start();
//                    } else {
//                        icon.setImageBitmap(thumb);
//                    }
//                } else {
                icon.setImageResource(R.drawable.pic_icon);
//                }

            } else if (sub_ext.equalsIgnoreCase("zip") ||
                    sub_ext.equalsIgnoreCase("gzip") ||
                    sub_ext.equalsIgnoreCase("gz")) {

                icon.setImageResource(R.drawable.zip_icon);

            } else if (sub_ext.equalsIgnoreCase("m4v") ||
                    sub_ext.equalsIgnoreCase("wmv") ||
                    sub_ext.equalsIgnoreCase("3gp") ||
                    sub_ext.equalsIgnoreCase("mp4")) {

                icon.setImageResource(R.drawable.video_icon);

            } else if (sub_ext.equalsIgnoreCase("doc") ||
                    sub_ext.equalsIgnoreCase("docx")) {

                icon.setImageResource(R.drawable.word_icon);

            } else if (sub_ext.equalsIgnoreCase("xls") ||
                    sub_ext.equalsIgnoreCase("xlsx")) {

                icon.setImageResource(R.drawable.excel_icon);

            } else if (sub_ext.equalsIgnoreCase("ppt") ||
                    sub_ext.equalsIgnoreCase("pptx")) {

                icon.setImageResource(R.drawable.ppt_icon);

            } else if (sub_ext.equalsIgnoreCase("html")) {
                icon.setImageResource(R.drawable.notfound_icon);

            } else if (sub_ext.equalsIgnoreCase("xml")) {
                icon.setImageResource(R.drawable.xml_icon);

            } else if (sub_ext.equalsIgnoreCase("conf")) {
                icon.setImageResource(R.drawable.notfound_icon);

            } else if (sub_ext.equalsIgnoreCase("apk")) {
                icon.setImageResource(R.drawable.apk_icon);

            } else if (sub_ext.equalsIgnoreCase("jar")) {
                icon.setImageResource(R.drawable.notfound_icon);

            } else {
                icon.setImageResource(R.drawable.txt_icon);
            }

        } else if (file != null && file.isDirectory()) {
            if (file.canRead() && file.list().length > 0)
                icon.setImageResource(R.drawable.file_icon);
            else
                icon.setImageResource(R.drawable.file_icon);
        }

        String permission = getFilePermissions(file);

        if (file.isFile()) {
            double size = file.length();
            if (size > GB)
                display_size = String.format("%.2f Gb ", (double) size / GB);
            else if (size < GB && size > MG)
                display_size = String.format("%.2f Mb ", (double) size / MG);
            else if (size < MG && size > KB)
                display_size = String.format("%.2f Kb ", (double) size / KB);
            else
                display_size = String.format("%.2f bytes ", (double) size);

            if (file.isHidden())
                bottomView.setText("(hidden) | " + display_size);
            else
                bottomView.setText(display_size);

        } else {
            if (file.isHidden())
                bottomView.setText("(hidden) | " + num_items + " items");
            else
                bottomView.setText(num_items + " items ");
        }

        String fileName = file.getName();

//        if (fileName.length() > 20) {
//            fileName = fileName.substring(0, 15) + "...";
//        }
        topView.setText(fileName);
        if (onItemClickListener != null) {
            holder.itemView.findViewById(R.id.cardView_bg).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition() - 1;
                    onItemClickListener.onItemClick(holder.itemView, pos);
                }
            });
            holder.itemView.findViewById(R.id.cardView_bg).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition() - 1;
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                    return true;
                }
            });
        }


    }

    public String getFilePermissions(File file) {
        String per = "-";

        if (file.isDirectory())
            per += "d";
        if (file.canRead())
            per += "r";
        if (file.canWrite())
            per += "w";

        return per;
    }

    /**
     * add item
     *
     * @param position
     */
    public void addData(int position) {
        //TODO

        notifyItemInserted(position);
    }

    public void upAllData(String path, ArrayList<String> dataSource) {
        this.path = path;

        mDataSource.clear();
        mDataSource.addAll(dataSource);

        mDataSource.add("");
        notifyDataSetChanged();

    }

    /**
     * remove item
     *
     * @param position
     */
    public void removeData(int position) {
        //TODO

        notifyItemRemoved(position);
    }


    public interface OnItemClickListener {

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


}