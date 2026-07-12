package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.databinding.YoutubeItemBinding;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class YouTubeListAdapter extends RecyclerView.Adapter<YouTubeListAdapter.YouTubeListHolder> {
    private final List<YouTubeItem> mYouTubeItemList = new ArrayList<>();

    private BiConsumer<View, Integer> mOnItemClickListener;

    @NonNull
    @Override
    public YouTubeListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new YouTubeListHolder(YoutubeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(YouTubeListHolder holder, int position) {
        holder.bind(mYouTubeItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mYouTubeItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(BiConsumer<View, Integer> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<YouTubeItem> getCurrentList() {
        return mYouTubeItemList;
    }

    public void submitList(List<YouTubeItem> youTubeItems) {
        mYouTubeItemList.clear();
        mYouTubeItemList.addAll(youTubeItems);
        notifyDataSetChanged();
    }

    public class YouTubeListHolder extends RecyclerView.ViewHolder {
        private final YoutubeItemBinding mBinding;

        public YouTubeListHolder(YoutubeItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.accept(v, getAdapterPosition());
            });
        }

        public void bind(YouTubeItem youTubeItem) {
            mBinding.setYouTubeItem(youTubeItem);
            mBinding.executePendingBindings();
        }
    }
}
