package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.helper.ZoomImageView;

import java.util.List;

public class PicturePagerAdapter extends PagerAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mImageList;

    public PicturePagerAdapter(Context context, List<String> images) {
        this.mContext = context;
        this.mImageList = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mInflater == null)
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.image_fullscreen, container, false);
        ZoomImageView zoomImageView = view.findViewById(R.id.ziv_image);
        String image = mImageList.get(position);

        Glide.with(mContext).load(image).into(zoomImageView);
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
