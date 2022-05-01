package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.GroupListItemBinding;
import com.hhp227.yu_minigroup.databinding.LoadMoreBinding;
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
                return new ItemHolder(GroupListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_LOADER:
                return new FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final GroupListItemBinding mBinding;

        public ItemHolder(GroupListItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(GroupItem groupItem) {
            mBinding.tvGroupName.setText(groupItem.getName());
            mBinding.tvGroupName.setMaxLines(NAME_MAX_LINE);
            mBinding.tvInfo.setText(groupItem.getJoinType().equals("0") ? "가입방식: 자동 승인" : "가입방식: 운영자 승인 확인");
            Glide.with(itemView.getContext())
                    .load(groupItem.getImage())
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .into(mBinding.ivGroupImage);
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
