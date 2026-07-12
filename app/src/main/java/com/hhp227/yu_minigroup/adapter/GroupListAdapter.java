package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.databinding.GroupListItemBinding;
import com.hhp227.yu_minigroup.databinding.LoadMoreBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_GROUP = 0;

    private static final int TYPE_LOADER = 1;

    private final Activity mActivity;

    private final List<Map.Entry<String, GroupItem>> mGroupItemList = new ArrayList<>(Collections.singletonList(null));

    private int mProgressBarVisibility, mButtonType;

    public GroupListAdapter(Activity activity) {
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP:
                return new ItemHolder(GroupListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_LOADER:
                return new FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mGroupItemList.get(position).getValue());
        } else if (holder instanceof FooterHolder) {
            ((FooterHolder) holder).bind(mProgressBarVisibility);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemList.get(position) != null ? TYPE_GROUP : TYPE_LOADER;
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;

        notifyDataSetChanged();
    }

    public void setButtonType(int type) {
        this.mButtonType = type;
    }

    public String getKey(int position) {
        return mGroupItemList.get(position).getKey();
    }

    public void submitList(List<Map.Entry<String, GroupItem>> groupItemList) {
        mGroupItemList.clear();
        mGroupItemList.add(null);
        mGroupItemList.addAll(mGroupItemList.size() - 1, groupItemList);
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final GroupListItemBinding mBinding;

        public ItemHolder(GroupListItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(GroupItem groupItem) {
            mBinding.setGroupItem(groupItem);
            mBinding.executePendingBindings();
            itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("grp_id", groupItem.getId());
                args.putString("grp_nm", groupItem.getName());
                args.putString("img", groupItem.getImage());
                args.putString("info", groupItem.getInfo());
                args.putString("desc", groupItem.getDescription());
                args.putString("type", groupItem.getJoinType());
                args.putInt("btn_type", mButtonType);
                args.putString("key", mGroupItemList.get(getAdapterPosition()).getKey());

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(((FragmentActivity) mActivity).getSupportFragmentManager(), "dialog");
            });
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final LoadMoreBinding mBinding;

        public FooterHolder(LoadMoreBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(int progressBarVisibility) {
            mBinding.pbMore.setVisibility(progressBarVisibility);
        }
    }
}
