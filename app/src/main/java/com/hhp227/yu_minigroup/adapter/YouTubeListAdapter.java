package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.List;

public class YouTubeListAdapter extends RecyclerView.Adapter<YouTubeListAdapter.YouTubeListHolder> {
    private Context mContext;
    private List<YouTubeItem> mYouTubeItemList;
    private OnItemClickListener mOnItemClickListener;

    public YouTubeListAdapter(Context mContext, List<YouTubeItem> mYouTubeItemList) {
        this.mContext = mContext;
        this.mYouTubeItemList = mYouTubeItemList;
    }

    @Override
    public YouTubeListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.youtube_item, parent, false);
        return new YouTubeListHolder(view);
    }

    @Override
    public void onBindViewHolder(YouTubeListHolder holder, int position) {
        YouTubeItem youTubeItem = mYouTubeItemList.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(v, position);
        });
        Glide.with(mContext)
                .load(youTubeItem.thumbnail)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(holder.imageView);
        holder.title.setText(youTubeItem.title);
        holder.channelTitle.setText(youTubeItem.channelTitle);
    }

    @Override
    public int getItemCount() {
        return mYouTubeItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public static class YouTubeListHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView title, channelTitle;

        public YouTubeListHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_youtube);
            title = itemView.findViewById(R.id.tv_title);
            channelTitle = itemView.findViewById(R.id.tv_channel_title);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
