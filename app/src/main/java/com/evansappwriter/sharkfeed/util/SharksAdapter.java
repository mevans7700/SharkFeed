package com.evansappwriter.sharkfeed.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.evansappwriter.sharkfeed.R;
import com.evansappwriter.sharkfeed.model.Photo;

import java.util.List;

/**
 * Created by markevans on 7/21/16.
 */
public class SharksAdapter extends RecyclerView.Adapter<SharksAdapter.ViewHolder>{
    private List<Photo> mDataset;
    private Context mContext;

    public SharksAdapter(Context context, List<Photo> dataSet){
        mDataset = dataSet;
        mContext = context;
    }

    public void showNew(List<Photo> dataSet) {
        if (dataSet == mDataset) {
            return;
        }

        mDataset = dataSet;

        // notify the observers about the new objects
        notifyDataSetChanged();
    }

    public void showOlder(List<Photo> dataSet) {
        if (dataSet == mDataset) {
            return;
        }

        if (dataSet.size() == 0) {
            return;
        }

        mDataset.addAll(dataSet);
        notifyDataSetChanged();
    }

    public Photo getItem(int position) {
        if (mDataset  != null) {
            return mDataset.get(position);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public ViewHolder(View v){
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.img_shark);
        }
    }

    @Override
    public SharksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.shark_thumbnail,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SharksAdapter.ViewHolder holder, int position) {
        Photo sp = mDataset.get(position);

        String url = "";
        if (!TextUtils.isEmpty(sp.getUrlThumb())) {
            url = sp.getUrlThumb();
        } else if (!TextUtils.isEmpty(sp.getUrlMedium())) {
            url = sp.getUrlMedium();
        } else if (!TextUtils.isEmpty(sp.getUrlLarge())) {
            url = sp.getUrlLarge();
        } else if ((!TextUtils.isEmpty(sp.getUrlOriginal()))) {
            url = sp.getUrlOriginal();
        }

        Glide.with(mContext)
                .load(url)
                .thumbnail(0.5f)
                .crossFade()
                .placeholder(R.drawable.default_photo)
                .error(R.drawable.default_photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private SharksAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final SharksAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
