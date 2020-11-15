package com.hhp227.yu_minigroup.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.List;
import java.util.Map;

public class WriteListAdapter extends RecyclerView.Adapter {
    private static final int TYPE_TEXT = 0;

    private static final int TYPE_CONTENT = 1;

    private final List<Object> mWriteItemList;

    private Map<String, Object> mTextMap;

    private HeaderHolder mHeaderHolder;

    public WriteListAdapter(List<Object> writeItemList) {
        this.mWriteItemList = writeItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.write_text, parent, false);
                mHeaderHolder = new HeaderHolder(headerView);
                return mHeaderHolder;
            case TYPE_CONTENT:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.write_content, parent, false);
                return new ItemHolder(itemView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((String) mTextMap.get("title"), (String) mTextMap.get("content"));
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

    public void addHeaderView(Map<String, Object> textMap) {
        this.mTextMap = textMap;

        mWriteItemList.add(textMap);
    }

    public Map<String, Object> getTextMap() {
        mTextMap.put("title", mHeaderHolder.inputTitle.getText().toString());
        mTextMap.put("content", mHeaderHolder.inputContent.getText());
        return mTextMap;
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        public TextView inputTitle, inputContent;

        public HeaderHolder(View itemView) {
            super(itemView);
            inputTitle = itemView.findViewById(R.id.et_title);
            inputContent = itemView.findViewById(R.id.et_content);
        }

        public void bind(String title, String content) {
            inputTitle.setText(title);
            inputContent.setText(content);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView, videoMark;

        public ItemHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image_preview);
            videoMark = itemView.findViewById(R.id.iv_video_preview);

            itemView.setOnClickListener(view -> {
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

                imageView.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
                Glide.with(itemView.getContext())
                        .load(bitmap)
                        .into(imageView);
                videoMark.setVisibility(View.GONE);
            } else if (object instanceof String) {
                String imageUrl = (String) mWriteItemList.get(getAdapterPosition());

                imageView.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE);
                Glide.with(itemView.getContext()).load(imageUrl).into(imageView);
                videoMark.setVisibility(View.GONE);
            } else if (object instanceof YouTubeItem) { // 수정
                YouTubeItem youTubeItem = (YouTubeItem) mWriteItemList.get(getAdapterPosition());

                //리팩토링 요망
                videoMark.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(youTubeItem.thumbnail)
                        .into(imageView);
            }
        }
    }
}
