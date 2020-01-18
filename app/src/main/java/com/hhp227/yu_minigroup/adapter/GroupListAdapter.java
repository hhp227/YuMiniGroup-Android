package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADER = 1;
    private static final int NAME_MAX_LINE = 2;
    private int mProgressBarVisibility, mButtonType;
    private Activity mActivity;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;

    public GroupListAdapter(Activity activity, List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        this.mActivity = activity;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                View itemView = LayoutInflater.from(mActivity).inflate(R.layout.group_list_item, parent, false);
                return new ItemHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(mActivity).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            GroupItem groupItem = mGroupItemValues.get(position);

            ((ItemHolder) holder).groupName.setText(groupItem.getName());
            ((ItemHolder) holder).groupName.setMaxLines(NAME_MAX_LINE);
            ((ItemHolder) holder).groupInfo.setText(groupItem.getJoinType().equals("0") ? "가입방식: 자동 승인" : "가입방식: 운영자 승인 확인");

            Glide.with(mActivity).load(groupItem.getImage()).apply(RequestOptions.errorOf(R.drawable.ic_launcher_background)).into(((ItemHolder) holder).groupImage);
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("grp_id", groupItem.getId());
                args.putString("grp_nm", groupItem.getName());
                args.putString("img", groupItem.getImage());
                args.putString("info", groupItem.getInfo());
                args.putString("desc", groupItem.getDescription());
                args.putString("type", groupItem.getJoinType());
                args.putInt("btn_type", mButtonType);
                args.putString("key", mGroupItemKeys.get(position));

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(((FragmentActivity) mActivity).getSupportFragmentManager(), "dialog");
            });
        } else if (holder instanceof FooterHolder) {
            ((FooterHolder) holder).progressBar.setVisibility(mProgressBarVisibility);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemValues.get(position) != null ? TYPE_ITEM : TYPE_LOADER;
    }

    public void addFooterView() {
        mGroupItemKeys.add("");
        mGroupItemValues.add(null);
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;
    }

    public void setButtonType(int type) {
        this.mButtonType = type;
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage;
        private TextView groupName, groupInfo;

        public ItemHolder(View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_group_name);
            groupInfo = itemView.findViewById(R.id.tv_info);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public FooterHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_more);
        }
    }
}
