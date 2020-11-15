package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_GROUP = 0;

    private static final int TYPE_LOADER = 1;

    private static final int NAME_MAX_LINE = 2;

    private final Activity mActivity;

    private final List<String> mGroupItemKeys;

    private final List<GroupItem> mGroupItemValues;

    private int mProgressBarVisibility, mButtonType;

    public GroupListAdapter(Activity activity, List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        this.mActivity = activity;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP:
                View itemView = LayoutInflater.from(mActivity).inflate(R.layout.group_list_item, parent, false);
                return new ItemHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(mActivity).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mGroupItemValues.get(position));
        } else if (holder instanceof FooterHolder) {
            ((FooterHolder) holder).bind(mProgressBarVisibility);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemValues.get(position) != null ? TYPE_GROUP : TYPE_LOADER;
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

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final ImageView groupImage;

        private final TextView groupName, groupInfo;

        public ItemHolder(View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_group_name);
            groupInfo = itemView.findViewById(R.id.tv_info);
        }

        public void bind(GroupItem groupItem) {
            groupName.setText(groupItem.getName());
            groupName.setMaxLines(NAME_MAX_LINE);
            groupInfo.setText(groupItem.getJoinType().equals("0") ? "가입방식: 자동 승인" : "가입방식: 운영자 승인 확인");
            Glide.with(itemView.getContext())
                    .load(groupItem.getImage())
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .into(groupImage);
            itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("grp_id", groupItem.getId());
                args.putString("grp_nm", groupItem.getName());
                args.putString("img", groupItem.getImage());
                args.putString("info", groupItem.getInfo());
                args.putString("desc", groupItem.getDescription());
                args.putString("type", groupItem.getJoinType());
                args.putInt("btn_type", mButtonType);
                args.putString("key", mGroupItemKeys.get(getAdapterPosition()));

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(((FragmentActivity) mActivity).getSupportFragmentManager(), "dialog");
            });
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;

        public FooterHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_more);
        }

        public void bind(int progressBarVisibility) {
            progressBar.setVisibility(progressBarVisibility);
        }
    }
}
