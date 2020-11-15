package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupPagerAdapter extends PagerAdapter {
    private final List<GroupItem> mGroupItemList;

    private LayoutInflater mInflater;

    public GroupPagerAdapter(List<GroupItem> groupItemList) {
        this.mGroupItemList = groupItemList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mInflater == null)
            mInflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.group_pager_item, container, false);
        GroupItem groupItem = mGroupItemList.get(position);
        ImageView groupImage = view.findViewById(R.id.iv_group_image);
        TextView groupName = view.findViewById(R.id.tv_group_name);

        Glide.with(view.getContext())
                .load(groupItem.getImage())
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                .into(groupImage);
        groupName.setText(groupItem.getName());
        container.addView(view);
        return view;
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
