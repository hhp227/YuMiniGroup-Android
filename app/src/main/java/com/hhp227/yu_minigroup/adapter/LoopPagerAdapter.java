package com.hhp227.yu_minigroup.adapter;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.FragmentMainPagerBinding;

import java.util.List;

public class LoopPagerAdapter extends PagerAdapter {
    private List<String> mPagerItemList;

    private View.OnClickListener mOnClickListener;

    public LoopPagerAdapter(List<String> pagerItemList) {
        this.mPagerItemList = pagerItemList;
    }

    public void setData(List<String> pagerItemList) {
        this.mPagerItemList = pagerItemList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        FragmentMainPagerBinding binding = FragmentMainPagerBinding.inflate(LayoutInflater.from(view.getContext()), view, false);
        switch (mPagerItemList.get(position)) {
            case "메인":
                binding.tvType1.setVisibility(View.GONE);
                binding.tvType2.setVisibility(View.GONE);
                binding.rlTypeMain.setVisibility(View.VISIBLE);
                binding.rlTypeImage.setVisibility(View.GONE);
                binding.bFind.setOnClickListener(mOnClickListener);
                binding.bCreate.setOnClickListener(mOnClickListener);
                break;
            case "이미지1":
                binding.tvType1.setVisibility(View.VISIBLE);
                binding.tvType2.setVisibility(View.GONE);
                binding.rlTypeMain.setVisibility(View.GONE);
                binding.rlTypeImage.setVisibility(View.VISIBLE);
                binding.ivBanner.setImageResource(R.drawable.banner01);
                break;
            case "이미지2":
                binding.tvType1.setVisibility(View.GONE);
                binding.tvType2.setVisibility(View.VISIBLE);
                binding.rlTypeMain.setVisibility(View.GONE);
                binding.rlTypeImage.setVisibility(View.VISIBLE);
                binding.ivBanner.setImageResource(R.drawable.banner02);
                break;
        }
        view.addView(binding.getRoot(), 0);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return mPagerItemList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup pager, int position, @NonNull Object view) {
        pager.removeView((View) view);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) { }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) { }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {}

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }
}
