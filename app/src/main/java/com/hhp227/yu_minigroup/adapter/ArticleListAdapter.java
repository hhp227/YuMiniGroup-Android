package com.hhp227.yu_minigroup.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;

import java.text.SimpleDateFormat;
import java.util.List;

public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;

    private static final int TYPE_LOADER = 1;

    private static final int CONTENT_MAX_LINE = 4;

    private final List<String> mArticleItemKeys;

    private final List<ArticleItem> mArticleItemValues;

    private int mProgressBarVisibility;

    private OnItemClickListener mOnItemClickListener;

    private String mGroupKey;

    public ArticleListAdapter(List<String> articleItemKeys, List<ArticleItem> articleItemValues, String groupKey) {
        this.mArticleItemKeys = articleItemKeys;
        this.mArticleItemValues = articleItemValues;
        this.mGroupKey = groupKey;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_item, parent, false);
                return new ItemHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mArticleItemValues.get(position));
        } else if (holder instanceof FooterHolder)
            ((FooterHolder) holder).bind(mProgressBarVisibility);
    }

    @Override
    public int getItemCount() {
        return mArticleItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mArticleItemValues.get(position) != null ? TYPE_ARTICLE : TYPE_LOADER;
    }

    public void addFooterView() {
        mArticleItemKeys.add("");
        mArticleItemValues.add(null);
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public String getKey(int position) {
        return mArticleItemKeys.get(position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage, articleImage, videoMark;

        private LinearLayout replyButton, likeButton;

        private final RelativeLayout imageContainer;

        private TextView title, timestamp, content, contentMore, replyCount, likeCount;

        ItemHolder(View itemView) {
            super(itemView);
            CardView article = itemView.findViewById(R.id.cv_article);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            title = itemView.findViewById(R.id.tv_title);
            timestamp = itemView.findViewById(R.id.tv_timestamp);
            content = itemView.findViewById(R.id.tv_content);
            contentMore = itemView.findViewById(R.id.tv_content_more);
            imageContainer = itemView.findViewById(R.id.rl_article_image);
            articleImage = itemView.findViewById(R.id.iv_article_image);
            videoMark = itemView.findViewById(R.id.iv_video_preview);
            replyCount = itemView.findViewById(R.id.tv_replycount);
            replyButton = itemView.findViewById(R.id.ll_reply);

            article.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, getAdapterPosition());
            });
            replyButton.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, getAdapterPosition());
            });
        }

        private void bind(ArticleItem articleItem) {
            Glide.with(itemView.getContext())
                    .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                            .build()) : null)
                    .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(profileImage);
            title.setText(articleItem.getName() != null ? articleItem.getTitle() + " - " + articleItem.getName() : articleItem.getTitle());
            timestamp.setText(articleItem.getDate() != null ? articleItem.getDate() : new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
            if (!TextUtils.isEmpty(articleItem.getContent())) {
                content.setText(articleItem.getContent());
                content.setMaxLines(CONTENT_MAX_LINE);
                content.setVisibility(View.VISIBLE);
            } else
                content.setVisibility(View.GONE);
            contentMore.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) && content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);
            if (articleItem.getYoutube() != null) {
                imageContainer.setVisibility(View.VISIBLE);
                videoMark.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(articleItem.getYoutube().thumbnail)
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(articleImage);
            } else if (articleItem.getImages() != null && articleItem.getImages().size() > 0) {
                imageContainer.setVisibility(View.VISIBLE);
                videoMark.setVisibility(View.INVISIBLE);
                Glide.with(itemView.getContext())
                        .load(articleItem.getImages().get(0))
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(articleImage);
            } else
                imageContainer.setVisibility(View.GONE);
            replyCount.setText(articleItem.getReplyCount());
            replyButton.setTag(getAdapterPosition());
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;

        FooterHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_more);
        }

        public void bind(int progressBarVisibility) {
            progressBar.setVisibility(progressBarVisibility);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
