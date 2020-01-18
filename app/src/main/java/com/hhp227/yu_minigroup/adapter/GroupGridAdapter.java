package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupGridAdapter extends RecyclerView.Adapter<GroupGridAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private OnItemClickListener mOnItemClickListener;

    public GroupGridAdapter(Context context, List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        this.mContext = context;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupItem groupItem = mGroupItemValues.get(position);
        if (!groupItem.isAd()) {
            holder.groupLayout.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            });
            Glide.with(mContext).load(groupItem.getImage()).transition(new DrawableTransitionOptions().crossFade(150)).into(holder.groupImage);
            holder.groupName.setText(groupItem.getName());
            holder.more.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_group, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_group_menu1:
                            Toast.makeText(mContext, "테스트1", Toast.LENGTH_LONG).show();
                            return true;
                        case R.id.action_group_menu2:
                            Toast.makeText(mContext, "테스트2", Toast.LENGTH_LONG).show();
                            return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
            holder.groupLayout.setVisibility(View.VISIBLE);
            holder.adView.setVisibility(View.GONE);
        } else {
            AdLoader.Builder builder = new AdLoader.Builder(mContext, mContext.getString(R.string.native_ad));
            builder.forUnifiedNativeAd(unifiedNativeAd -> {
                holder.mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.adView.setMediaView(holder.mediaView);
                holder.adView.setHeadlineView(holder.headlineView);
                holder.adView.setBodyView(holder.bodyView);
                holder.adView.setAdvertiserView(holder.advertiser);
                holder.headlineView.setText(unifiedNativeAd.getHeadline());
                holder.adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                if (unifiedNativeAd.getBody() != null) {
                    holder.bodyView.setText(unifiedNativeAd.getBody());
                    holder.adView.getBodyView().setVisibility(View.VISIBLE);
                } else
                    holder.adView.getBodyView().setVisibility(View.INVISIBLE);
                if (unifiedNativeAd.getAdvertiser() != null) {
                    holder.advertiser.setText(unifiedNativeAd.getAdvertiser());
                    holder.adView.getAdvertiserView().setVisibility(View.VISIBLE);
                } else
                    holder.adView.getAdvertiserView().setVisibility(View.GONE);

                holder.adView.setNativeAd(unifiedNativeAd);
                holder.mediaView.addView(getAdText());
            });
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Toast.makeText(mContext, "광고", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Toast.makeText(mContext, "광고 불러오기 실패 : " + i, Toast.LENGTH_LONG).show();
                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
            holder.groupLayout.setVisibility(View.GONE);
            holder.adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    private TextView getAdText() {
        TextView adText = new TextView(mContext);
        adText.setText(mContext.getString(R.string.ad_attribution));
        adText.setTextSize(12);
        adText.setBackgroundColor(mContext.getResources().getColor(R.color.bg_ad_attribution));
        adText.setTextColor(mContext.getResources().getColor(R.color.txt_ad_attribution));
        adText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        adText.setGravity(Gravity.CENTER_VERTICAL);
        return adText;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage, more;
        private MediaView mediaView;
        private RelativeLayout groupLayout;
        private TextView groupName, headlineView, bodyView, advertiser;
        private UnifiedNativeAdView adView;

        public ViewHolder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.unav);
            groupLayout = itemView.findViewById(R.id.rl_group);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_title);
            more = itemView.findViewById(R.id.iv_more);
            mediaView = itemView.findViewById(R.id.ad_media);
            headlineView = itemView.findViewById(R.id.ad_headline);
            bodyView = itemView.findViewById(R.id.ad_body);
            advertiser = itemView.findViewById(R.id.ad_advertiser);
        }
    }
}