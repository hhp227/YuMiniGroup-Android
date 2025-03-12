package com.hhp227.yu_minigroup.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.databinding.WriteContentBinding;
import com.hhp227.yu_minigroup.databinding.WriteTextBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.List;
import java.util.Map;

// TODO ViewHolder만 개선하면 됨
public class WriteListAdapter extends RecyclerView.Adapter {
    private static final int TYPE_TEXT = 0;

    private static final int TYPE_CONTENT = 1;

    private final List<Object> mWriteItemList;

    public WriteListAdapter(List<Object> writeItemList) {
        this.mWriteItemList = writeItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                return new HeaderHolder(WriteTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_CONTENT:
                return new ItemHolder(WriteContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((Map<String, MutableLiveData<String>>) mWriteItemList.get(position));
        } else if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mWriteItemList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mWriteItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position < 1 ? TYPE_TEXT : TYPE_CONTENT;
    }

    public Map<String, MutableLiveData<String>> getHeaderItem() {
        return (Map<String, MutableLiveData<String>>) mWriteItemList.get(0);
    }

    public List<Object> getItemList() {
        return mWriteItemList;
    }

    public void submitList(List<Object> contentList) {
        mWriteItemList.clear();
        mWriteItemList.addAll(contentList);
        notifyDataSetChanged();
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private final WriteTextBinding mBinding;

        public HeaderHolder(WriteTextBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(Map<String, MutableLiveData<String>> headerItem) {
            mBinding.setItem(headerItem);
            mBinding.executePendingBindings();
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final WriteContentBinding mBinding;

        public ItemHolder(WriteContentBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.ivImagePreview.setOnClickListener(view -> {
                view.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle("작업 선택");
                    menu.add(0, getAdapterPosition(), Menu.NONE, "삭제");
                });
                view.showContextMenu();
            });
        }

        public void bind(Object object) {
            if (object instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) mWriteItemList.get(getAdapterPosition());

                mBinding.ivImagePreview.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
                Glide.with(itemView.getContext())
                        .load(bitmap)
                        .into(mBinding.ivImagePreview);
                mBinding.ivVideoPreview.setVisibility(View.GONE);
            } else if (object instanceof String) {
                String imageUrl = (String) mWriteItemList.get(getAdapterPosition());

                mBinding.ivImagePreview.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE);
                Glide.with(itemView.getContext()).load(imageUrl).into(mBinding.ivImagePreview);
                mBinding.ivVideoPreview.setVisibility(View.GONE);
            } else if (object instanceof YouTubeItem) { // 수정
                YouTubeItem youTubeItem = (YouTubeItem) mWriteItemList.get(getAdapterPosition());

                //리팩토링 요망
                mBinding.ivVideoPreview.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(youTubeItem.thumbnail)
                        .into(mBinding.ivImagePreview);
            }
        }
    }
}
