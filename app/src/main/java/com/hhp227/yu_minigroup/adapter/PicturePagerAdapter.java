package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.helper.ZoomImageView;

import java.util.List;

public class PicturePagerAdapter extends PagerAdapter {
    private final List<String> mImageList;

    private LayoutInflater mInflater;

    public PicturePagerAdapter(List<String> images) {
        this.mImageList = images;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mInflater == null)
            mInflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.image_fullscreen, container, false);
        ZoomImageView zoomImageView = view.findViewById(R.id.ziv_image);
        String image = mImageList.get(position);

        Glide.with(view.getContext()).load(image).into(zoomImageView);
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
