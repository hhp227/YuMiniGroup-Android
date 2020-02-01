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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupGridAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_GROUP = 1;

    private Context mContext;
    private List<String> mGroupItemKeys;
    private List<Object> mGroupItemValues;
    private OnItemClickListener mOnItemClickListener;

    public GroupGridAdapter(Context context, List<String> groupItemKeys, List<Object> groupItemValues) {
        this.mContext = context;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_header, parent, false);
                return new HeaderHolder(headerView);
            case TYPE_GROUP:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_item, parent, false);
                return new ItemHolder(itemView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            Map<String, String> map = (Map<String, String>) mGroupItemValues.get(position);
            ((HeaderHolder) holder).text.setText(map.get("text"));
        } else if (holder instanceof ItemHolder) {
            GroupItem groupItem = (GroupItem) mGroupItemValues.get(position);
            if (!groupItem.isAd()) {
                ((ItemHolder) holder).groupLayout.setOnClickListener(v -> {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, position);
                });
                Glide.with(mContext).load(groupItem.getImage()).transition(new DrawableTransitionOptions().crossFade(150)).into(((ItemHolder) holder).groupImage);
                ((ItemHolder) holder).groupName.setText(groupItem.getName());
                ((ItemHolder) holder).more.setOnClickListener(v -> {
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
                ((ItemHolder) holder).groupLayout.setVisibility(View.VISIBLE);
                ((ItemHolder) holder).adView.setVisibility(View.GONE);
            } else {
                AdLoader.Builder builder = new AdLoader.Builder(mContext, mContext.getString(R.string.native_ad));
                builder.forUnifiedNativeAd(unifiedNativeAd -> {
                    ((ItemHolder) holder).mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    ((ItemHolder) holder).adView.setMediaView(((ItemHolder) holder).mediaView);
                    ((ItemHolder) holder).adView.setHeadlineView(((ItemHolder) holder).headlineView);
                    ((ItemHolder) holder).adView.setBodyView(((ItemHolder) holder).bodyView);
                    ((ItemHolder) holder).adView.setAdvertiserView(((ItemHolder) holder).advertiser);
                    ((ItemHolder) holder).headlineView.setText(unifiedNativeAd.getHeadline());
                    ((ItemHolder) holder).adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                    if (unifiedNativeAd.getBody() != null) {
                        ((ItemHolder) holder).bodyView.setText(unifiedNativeAd.getBody());
                        ((ItemHolder) holder).adView.getBodyView().setVisibility(View.VISIBLE);
                    } else
                        ((ItemHolder) holder).adView.getBodyView().setVisibility(View.INVISIBLE);
                    if (unifiedNativeAd.getAdvertiser() != null) {
                        ((ItemHolder) holder).advertiser.setText(unifiedNativeAd.getAdvertiser());
                        ((ItemHolder) holder).adView.getAdvertiserView().setVisibility(View.VISIBLE);
                    } else
                        ((ItemHolder) holder).adView.getAdvertiserView().setVisibility(View.GONE);

                    ((ItemHolder) holder).adView.setNativeAd(unifiedNativeAd);
                    ((ItemHolder) holder).mediaView.addView(getAdText());
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
                ((ItemHolder) holder).groupLayout.setVisibility(View.GONE);
                ((ItemHolder) holder).adView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemValues.get(position) instanceof GroupItem ? TYPE_GROUP : TYPE_TEXT;
    }

    @Override
    public long getItemId(int position) {
        return position;
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

    public void addHeaderView(String text) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("text", text);
        mGroupItemKeys.add(0, text);
        mGroupItemValues.add(0, headerMap);
        notifyItemInserted(0);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView text;

        public HeaderHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_title);
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage, more;
        private MediaView mediaView;
        private RelativeLayout groupLayout;
        private TextView groupName, headlineView, bodyView, advertiser;
        private UnifiedNativeAdView adView;

        public ItemHolder(View itemView) {
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

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}