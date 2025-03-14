package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.databinding.ImageFullscreenBinding;

import java.util.ArrayList;
import java.util.List;

public class PicturePagerAdapter extends PagerAdapter {
    private final List<String> mImageList = new ArrayList<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageFullscreenBinding binding = ImageFullscreenBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        String image = mImageList.get(position);

        Glide.with(container.getContext()).load(image).into(binding.zivImage);
        container.addView(binding.getRoot());
        return binding.getRoot();
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

    public void submitList(List<String> list) {
        mImageList.clear();
        mImageList.addAll(list);
        notifyDataSetChanged();
    }
}
