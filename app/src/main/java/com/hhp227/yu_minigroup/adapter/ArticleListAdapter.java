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
    private static final int TYPE_ITEM = 0;
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
            case TYPE_ITEM:
                View itemView = LayoutInflater.from(mActivity).inflate(R.layout.article_item, parent, false);
                return new ArticleItemViewHolder(itemView);
            case TYPE_LOADER:
                View footerView = LayoutInflater.from(mActivity).inflate(R.layout.load_more, parent, false);
                return new FooterHolder(footerView);
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ArticleItemViewHolder) {
            ArticleItem articleItem = mArticleItemValues.get(position);

            ((ArticleItemViewHolder) holder).article.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
            Glide.with(mActivity)
                    .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                            .build()) : null)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(((ArticleItemViewHolder) holder).profileImage);
            ((ArticleItemViewHolder) holder).title.setText(articleItem.getName() != null ? articleItem.getTitle() + " - " + articleItem.getName() : articleItem.getTitle());
            ((ArticleItemViewHolder) holder).timestamp.setText(articleItem.getDate() != null ? articleItem.getDate() : new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
            if (!TextUtils.isEmpty(articleItem.getContent())) {
                ((ArticleItemViewHolder) holder).content.setText(articleItem.getContent());
                ((ArticleItemViewHolder) holder).content.setMaxLines(CONTENT_MAX_LINE);
                ((ArticleItemViewHolder) holder).content.setVisibility(View.VISIBLE);
            } else
                ((ArticleItemViewHolder) holder).content.setVisibility(View.GONE);

            ((ArticleItemViewHolder) holder).contentMore.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) && ((ArticleItemViewHolder) holder).content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);
            if (articleItem.getImages() != null && articleItem.getImages().size() > 0) {
                ((ArticleItemViewHolder) holder).articleImage.setVisibility(View.VISIBLE);
                Glide.with(mActivity)
                        .load(articleItem.getImages().get(0))
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(((ArticleItemViewHolder) holder).articleImage);
            } else
                ((ArticleItemViewHolder) holder).articleImage.setVisibility(View.GONE);
            ((ArticleItemViewHolder) holder).replyCount.setText(articleItem.getReplyCount());

            ((ArticleItemViewHolder) holder).replyButton.setTag(position);
            ((ArticleItemViewHolder) holder).replyButton.setOnClickListener(v -> {
                Intent intent = new Intent(mActivity, ArticleActivity.class);
                intent.putExtra("grp_id", Tab1Fragment.mGroupId);
                intent.putExtra("grp_nm", Tab1Fragment.mGroupName);
                intent.putExtra("artl_num", articleItem.getId());
                intent.putExtra("position", position + 1);
                intent.putExtra("auth", articleItem.isAuth());
                intent.putExtra("isbottom", true);
                intent.putExtra("grp_key", mGroupKey);
                intent.putExtra("artl_key", getKey(position));
                mActivity.startActivityForResult(intent, UPDATE_ARTICLE);
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
        return mArticleItemValues.get(position) != null ? TYPE_ITEM : TYPE_LOADER;
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

    public static class ArticleItemViewHolder extends RecyclerView.ViewHolder {
        private CardView article;
        private ImageView profileImage, articleImage;
        private LinearLayout replyButton, likeButton;
        private TextView title, timestamp, content, contentMore, replyCount, likeCount;

        public ArticleItemViewHolder(View itemView) {
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
