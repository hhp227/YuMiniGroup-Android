package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.ArticleActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.fragment.Tab1Fragment;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.hhp227.yu_minigroup.fragment.Tab1Fragment.UPDATE_ARTICLE;

public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_LOADER = 1;
    private static final int CONTENT_MAX_LINE = 4;
    private int mProgressBarVisibility;
    private Activity mActivity;
    private List<String> mArticleItemKeys;
    private List<ArticleItem> mArticleItemValues;
    private OnItemClickListener mOnItemClickListener;
    private String mGroupKey;

    public ArticleListAdapter(Activity activity, List<String> articleItemKeys, List<ArticleItem> articleItemValues, String groupKey) {
        this.mActivity = activity;
        this.mArticleItemKeys = articleItemKeys;
        this.mArticleItemValues = articleItemValues;
        this.mGroupKey = groupKey;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE:
                View itemView = LayoutInflater.from(mActivity).inflate(R.layout.article_item, parent, false);
                return new ArticleItemHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(mActivity).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ArticleItemHolder) {
            ArticleItem articleItem = mArticleItemValues.get(position);

            ((ArticleItemHolder) holder).article.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
            Glide.with(mActivity)
                    .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                            .build()) : null)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(((ArticleItemHolder) holder).profileImage);
            ((ArticleItemHolder) holder).title.setText(articleItem.getName() != null ? articleItem.getTitle() + " - " + articleItem.getName() : articleItem.getTitle());
            ((ArticleItemHolder) holder).timestamp.setText(articleItem.getDate() != null ? articleItem.getDate() : new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
            if (!TextUtils.isEmpty(articleItem.getContent())) {
                ((ArticleItemHolder) holder).content.setText(articleItem.getContent());
                ((ArticleItemHolder) holder).content.setMaxLines(CONTENT_MAX_LINE);
                ((ArticleItemHolder) holder).content.setVisibility(View.VISIBLE);
            } else
                ((ArticleItemHolder) holder).content.setVisibility(View.GONE);

            ((ArticleItemHolder) holder).contentMore.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) && ((ArticleItemHolder) holder).content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);
            if (articleItem.getImages() != null && articleItem.getImages().size() > 0) {
                ((ArticleItemHolder) holder).articleImage.setVisibility(View.VISIBLE);
                Glide.with(mActivity)
                        .load(articleItem.getImages().get(0))
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(((ArticleItemHolder) holder).articleImage);
            } else
                ((ArticleItemHolder) holder).articleImage.setVisibility(View.GONE);
            ((ArticleItemHolder) holder).replyCount.setText(articleItem.getReplyCount());

            ((ArticleItemHolder) holder).replyButton.setTag(position);
            ((ArticleItemHolder) holder).replyButton.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
        } else if (holder instanceof FooterHolder)
            ((FooterHolder) holder).progressBar.setVisibility(mProgressBarVisibility);
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

    public static class ArticleItemHolder extends RecyclerView.ViewHolder {
        private CardView article;
        private ImageView profileImage, articleImage;
        private LinearLayout replyButton, likeButton;
        private TextView title, timestamp, content, contentMore, replyCount, likeCount;

        public ArticleItemHolder(View itemView) {
            super(itemView);
            article = itemView.findViewById(R.id.cv_article);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            title = itemView.findViewById(R.id.tv_title);
            timestamp = itemView.findViewById(R.id.tv_timestamp);
            content = itemView.findViewById(R.id.tv_content);
            contentMore = itemView.findViewById(R.id.tv_content_more);
            articleImage = itemView.findViewById(R.id.iv_article_image);
            replyCount = itemView.findViewById(R.id.tv_replycount);
            replyButton = itemView.findViewById(R.id.ll_reply);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public FooterHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_more);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}