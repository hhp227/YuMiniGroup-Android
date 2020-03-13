package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.List;
import java.util.Map;

public class WriteListAdapter extends RecyclerView.Adapter {
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_CONTENT = 1;
    private Context mContext;
    private List<Object> mWriteItemList;
    private Map<String, Object> mTextMap;
    private HeaderHolder mHeaderHolder;

    public WriteListAdapter(Context context, List<Object> writeItemList) {
        this.mContext = context;
        this.mWriteItemList = writeItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                View headerView = LayoutInflater.from(mContext).inflate(R.layout.write_text, parent, false);
                mHeaderHolder = new HeaderHolder(headerView);
                return mHeaderHolder;
            case TYPE_CONTENT:
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.write_content, parent, false);
                return new ItemHolder(itemView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            String title = (String) mTextMap.get("title");
            String content = (String) mTextMap.get("content");
            ((HeaderHolder) holder).inputTitle.setText(title);
            ((HeaderHolder) holder).inputContent.setText(content);
        } else if (holder instanceof ItemHolder) {
            if (mWriteItemList.get(position) instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) mWriteItemList.get(position);
                ((ItemHolder) holder).imageView.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
                Glide.with(mContext).load(bitmap).into(((ItemHolder) holder).imageView);
                ((ItemHolder) holder).videoMark.setVisibility(View.GONE);
            } else if (mWriteItemList.get(position) instanceof String) {
                String imageUrl = (String) mWriteItemList.get(position);
                ((ItemHolder) holder).imageView.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE);
                Glide.with(mContext).load(imageUrl).into(((ItemHolder) holder).imageView);
                ((ItemHolder) holder).videoMark.setVisibility(View.GONE);
            } else if (mWriteItemList.get(position) instanceof YouTubeItem) { // 수정
                YouTubeItem youTubeItem = (YouTubeItem) mWriteItemList.get(position);
                //리팩토링 요망
                ((ItemHolder) holder).videoMark.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(youTubeItem.thumbnail).into(((ItemHolder) holder).imageView);
            }
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
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView imageView, videoMark;

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
    }
}
