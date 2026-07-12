package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hhp227.yu_minigroup.databinding.GroupPagerItemBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupPagerAdapter extends PagerAdapter {
    private final List<GroupItem> mGroupItemList;

    public GroupPagerAdapter(List<GroupItem> groupItemList) {
        this.mGroupItemList = groupItemList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        GroupPagerItemBinding binding = GroupPagerItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

        binding.setGroupItem(mGroupItemList.get(position));
        binding.executePendingBindings();
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return mGroupItemList.size();
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
