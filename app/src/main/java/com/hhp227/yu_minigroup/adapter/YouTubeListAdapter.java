package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.List;

public class YouTubeListAdapter extends RecyclerView.Adapter<YouTubeListAdapter.YouTubeListHolder> {
    private final List<YouTubeItem> mYouTubeItemList;

    private OnItemClickListener mOnItemClickListener;

    public YouTubeListAdapter(List<YouTubeItem> mYouTubeItemList) {
        this.mYouTubeItemList = mYouTubeItemList;
    }

    @NonNull
    @Override
    public YouTubeListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_item, parent, false);
        return new YouTubeListHolder(view);
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public class YouTubeListHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        private final TextView title, channelTitle;

        public YouTubeListHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_youtube);
            title = itemView.findViewById(R.id.tv_title);
            channelTitle = itemView.findViewById(R.id.tv_channel_title);

            itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, getAdapterPosition());
            });
        }

        public void bind(YouTubeItem youTubeItem) {
            Glide.with(itemView.getContext())
                    .load(youTubeItem.thumbnail)
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(imageView);
            title.setText(youTubeItem.title);
            channelTitle.setText(youTubeItem.channelTitle);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
