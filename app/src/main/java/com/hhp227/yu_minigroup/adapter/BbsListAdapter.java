package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.databinding.BbsItemBinding;
import com.hhp227.yu_minigroup.dto.BbsItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class BbsListAdapter extends RecyclerView.Adapter<BbsListAdapter.BbsListHolder> {
    private final List<BbsItem> mCurrentList = new ArrayList<>();

    private BiConsumer<View, Integer> mOnItemClickListener;

    @NonNull
    @Override
    public BbsListAdapter.BbsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BbsListHolder(BbsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(BbsListAdapter.BbsListHolder holder, int position) {
        holder.bind(mCurrentList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size();
    }

    public void setOnItemClickListener(BiConsumer<View, Integer> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<BbsItem> getCurrentList() {
        return mCurrentList;
    }

    public void submitList(List<BbsItem> list) {
        mCurrentList.clear();
        mCurrentList.addAll(list);
        notifyDataSetChanged();
    }

    public class BbsListHolder extends RecyclerView.ViewHolder {
        private final BbsItemBinding mBinding;

        public BbsListHolder(BbsItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.cardView.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.accept(v, getAdapterPosition());
            });
        }

        public void bind(BbsItem bbsItem) {
            mBinding.setBbsItem(bbsItem);
            mBinding.executePendingBindings();
        }
    }
}
