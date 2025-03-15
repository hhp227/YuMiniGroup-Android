package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.LoadMoreBinding;
import com.hhp227.yu_minigroup.databinding.MemberItemBinding;
import com.hhp227.yu_minigroup.dto.MemberItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MemberGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MEMBER = 0;

    private static final int TYPE_LOADER = 1;

    private int mProgressBarVisibility;

    private final List<MemberItem> mCurrentList = new ArrayList<>();

    private BiConsumer<View, Integer> mOnItemClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MEMBER:
                return new MemberGridHolder(MemberItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_LOADER:
                return new FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MemberGridHolder) {
            ((MemberGridHolder) holder).bind(mCurrentList.get(position));
        } else if (holder instanceof FooterHolder)
            ((FooterHolder) holder).bind(mProgressBarVisibility);
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;

        notifyItemChanged(getItemCount() - 1);
    }

    public void setOnItemClickListener(BiConsumer<View, Integer> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<MemberItem> getCurrentList() {
        return mCurrentList;
    }

    public void submitList(List<MemberItem> memberItemList) {
        mCurrentList.clear();
        mCurrentList.addAll(memberItemList);
        notifyDataSetChanged();
    }

    public class MemberGridHolder extends RecyclerView.ViewHolder {
        private final MemberItemBinding mBinding;

        public MemberGridHolder(MemberItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.accept(v, getAdapterPosition());
            });
        }

        public void bind(MemberItem memberItem) {
            mBinding.tvName.setText(memberItem.name);
            Glide.with(itemView.getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                            .build()))
                    .apply(new RequestOptions().centerCrop()
                            .error(R.drawable.user_image_view)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mBinding.ivProfileImage);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final LoadMoreBinding mBinding;

        FooterHolder(LoadMoreBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(int progressBarVisibility) {
            mBinding.pbMore.setVisibility(progressBarVisibility);
        }
    }
}
