package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
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
        GroupItem groupItem = mGroupItemList.get(position);

        Glide.with(container.getContext())
                .load(groupItem.getImage())
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                .into(binding.ivGroupImage);
        binding.tvGroupName.setText(groupItem.getName());
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
