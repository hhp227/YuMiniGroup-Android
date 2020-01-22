package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ArticleListViewHolder> {
    private static final int CONTENT_MAX_LINE = 4;
    private Activity mActivity;
    private List<String> mArticleItemKeys;
    private List<ArticleItem> mArticleItemValues;
    private String mGroupKey;

    public ArticleListAdapter(Activity activity, List<String> articleItemKeys, List<ArticleItem> articleItemValues, String groupKey) {
        this.mActivity = activity;
        this.mArticleItemKeys = articleItemKeys;
        this.mArticleItemValues = articleItemValues;
        this.mGroupKey = groupKey;
    }

    @Override
    public ArticleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.article_item, parent, false);
        return new ArticleListAdapter.ArticleListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArticleListViewHolder holder, int position) {
        ArticleItem articleItem = mArticleItemValues.get(position);

        Glide.with(mActivity)
                .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                        .build()) : null)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(holder.profileImage);
        holder.title.setText(articleItem.getName() != null ? articleItem.getTitle() + " - " + articleItem.getName() : articleItem.getTitle());
        holder.timestamp.setText(articleItem.getDate() != null ? articleItem.getDate() : new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
        if (!TextUtils.isEmpty(articleItem.getContent())) {
            holder.content.setText(articleItem.getContent());
            holder.content.setMaxLines(CONTENT_MAX_LINE);
            holder.content.setVisibility(View.VISIBLE);
        } else
            holder.content.setVisibility(View.GONE);

        holder.contentMore.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) && holder.content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);
        if (articleItem.getImages() != null && articleItem.getImages().size() > 0) {
            holder.articleImage.setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(articleItem.getImages().get(0))
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(holder.articleImage);
        } else
            holder.articleImage.setVisibility(View.GONE);
        holder.replyCount.setText(articleItem.getReplyCount());

        holder.replyButton.setTag(position);
        holder.replyButton.setOnClickListener(v -> {
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
    }

    @Override
    public int getItemCount() {
        return mArticleItemValues.size();
    }

    public String getKey(int position) {
        return mArticleItemKeys.get(position);
    }

    public static class ArticleListViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage, articleImage;
        private LinearLayout replyButton, likeButton;
        private TextView title, timestamp, content, contentMore, replyCount, likeCount;

        public ArticleListViewHolder(View itemView) {
            super(itemView);
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
}
