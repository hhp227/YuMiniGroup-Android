package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.ui.loopviewpager.LoopViewPager;
import com.hhp227.yu_minigroup.helper.ui.pageindicator.LoopingCirclePageIndicator;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupGridAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TEXT = 0;

    public static final int TYPE_GROUP = 1;

    public static final int TYPE_AD = 2;

    public static final int TYPE_BANNER = 3;

    public static final int TYPE_VIEW_PAGER = 4;

    private static final String TAG = "어뎁터";

    private final List<String> mGroupItemKeys;

    private final List<Object> mGroupItemValues;

    private OnItemClickListener mOnItemClickListener;

    private LoopViewPager mLoopViewPager;

    private LoopPagerAdapter mLoopPagerAdapter;

    private View.OnClickListener mOnClickListener;

    public GroupGridAdapter(List<String> groupItemKeys, List<Object> groupItemValues) {
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_header, parent, false);
                return new HeaderHolder(headerView);
            case TYPE_GROUP:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_item, parent, false);
                return new ItemHolder(itemView);
            case TYPE_AD:
                View adView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_ad, parent, false);
                return new AdHolder(adView);
            case TYPE_BANNER:
                View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_no_item, parent, false);
                return new BannerHolder(bannerView);
            case TYPE_VIEW_PAGER:
                View popularView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_view_pager, parent, false);
                return new ViewPagerHolder(popularView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((Map<String, String>) mGroupItemValues.get(position));
        } else if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind((GroupItem) mGroupItemValues.get(position));
        } else if (holder instanceof AdHolder) {
            ((AdHolder) holder).bind();
        } else if (holder instanceof BannerHolder) {
            mLoopPagerAdapter = new LoopPagerAdapter(Stream.<String>builder().add("메인").add("이미지1").add("이미지2").build().collect(Collectors.toList()));//
            mLoopViewPager = ((BannerHolder) holder).loopViewPager;

            mLoopViewPager.setAdapter(mLoopPagerAdapter);
            mLoopPagerAdapter.setOnClickListener(mOnClickListener);
            ((BannerHolder) holder).circlePageIndicator.setViewPager(mLoopViewPager);
        } else if (holder instanceof ViewPagerHolder) {
            ((ViewPagerHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemValues.get(position) instanceof Map ? TYPE_TEXT
                : mGroupItemValues.get(position) instanceof GroupItem ? TYPE_GROUP
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("광고") ? TYPE_AD
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("없음") ? TYPE_BANNER
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("뷰페이져") ? TYPE_VIEW_PAGER
                : -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private TextView getAdText(Context context) {
        TextView adText = new TextView(context);

        adText.setText(context.getString(R.string.ad_attribution));
        adText.setTextSize(12);
        adText.setBackgroundColor(context.getResources().getColor(R.color.bg_ad_attribution));
        adText.setTextColor(context.getResources().getColor(R.color.txt_ad_attribution));
        adText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        adText.setGravity(Gravity.CENTER_VERTICAL);
        return adText;
    }

    public void addHeaderView(String text) {
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("text", text);
        mGroupItemKeys.add(text);
        mGroupItemValues.add(headerMap);
        notifyItemInserted(mGroupItemValues.size() - 1);
    }

    public void addHeaderView(String text, int position) {
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("text", text);
        mGroupItemKeys.add(position, text);
        mGroupItemValues.add(position, headerMap);
        notifyItemInserted(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public void moveSliderPager() {
        if (mLoopViewPager == null || mLoopPagerAdapter.getCount() <= 0) {
            return;
        }

        LoopViewPager loopViewPager = mLoopViewPager;
        loopViewPager.setCurrentItem(loopViewPager.getCurrentItem() + 1);
    }

    private static int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private final TextView text;

        HeaderHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_title);
        }

        public void bind(Map<String, String> map) {
            text.setText(map.get("text"));
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final ImageView groupImage, more;

        private final RelativeLayout groupLayout;

        private final TextView groupName;

        ItemHolder(View itemView) {
            super(itemView);
            groupLayout = itemView.findViewById(R.id.rl_group);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_title);
            more = itemView.findViewById(R.id.iv_more);

            groupLayout.setOnClickListener(v -> {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, getAdapterPosition());
            });
            more.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v);
                MenuInflater inflater = popupMenu.getMenuInflater();

                inflater.inflate(R.menu.menu_group, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_group_menu1:
                            Toast.makeText(itemView.getContext(), "테스트1", Toast.LENGTH_LONG).show();
                            return true;
                        case R.id.action_group_menu2:
                            Toast.makeText(itemView.getContext(), "테스트2", Toast.LENGTH_LONG).show();
                            return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }

        public void bind(GroupItem groupItem) {
            Glide.with(itemView.getContext())
                    .load(groupItem.getImage())
                    .transition(new DrawableTransitionOptions().crossFade(150))
                    .into(groupImage);
            groupName.setText(groupItem.getName());
            groupLayout.setVisibility(View.VISIBLE);
        }
    }

    public class AdHolder extends RecyclerView.ViewHolder {
        private final MediaView mediaView;

        private final TextView headlineView, bodyView, advertiser;

        private final UnifiedNativeAdView adView;

        AdHolder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.unav);
            mediaView = itemView.findViewById(R.id.ad_media);
            headlineView = itemView.findViewById(R.id.ad_headline);
            bodyView = itemView.findViewById(R.id.ad_body);
            advertiser = itemView.findViewById(R.id.ad_advertiser);
        }

        public void bind() {
            AdLoader.Builder builder = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad));
            builder.forUnifiedNativeAd(unifiedNativeAd -> {
                mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                adView.setMediaView(mediaView);
                adView.setHeadlineView(headlineView);
                adView.setBodyView(bodyView);
                adView.setAdvertiserView(advertiser);
                headlineView.setText(unifiedNativeAd.getHeadline());
                adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                if (unifiedNativeAd.getBody() != null) {
                    bodyView.setText(unifiedNativeAd.getBody());
                    adView.getBodyView().setVisibility(View.VISIBLE);
                } else
                    adView.getBodyView().setVisibility(View.INVISIBLE);
                if (unifiedNativeAd.getAdvertiser() != null) {
                    advertiser.setText(unifiedNativeAd.getAdvertiser());
                    adView.getAdvertiserView().setVisibility(View.VISIBLE);
                } else
                    adView.getAdvertiserView().setVisibility(View.GONE);
                adView.setNativeAd(unifiedNativeAd);
                mediaView.addView(getAdText(itemView.getContext()));
            });
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Toast.makeText(itemView.getContext(), "광고", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Toast.makeText(itemView.getContext(), "광고 불러오기 실패 : " + i, Toast.LENGTH_LONG).show();
                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
            adView.setVisibility(View.VISIBLE);
        }
    }

    public static class BannerHolder extends RecyclerView.ViewHolder {
        private final LoopingCirclePageIndicator circlePageIndicator;

        private final LoopViewPager loopViewPager;

        BannerHolder(View itemView) {
            super(itemView);
            circlePageIndicator = itemView.findViewById(R.id.cpi_theme_slider_indicator);
            loopViewPager = itemView.findViewById(R.id.lvp_theme_slider_pager);
        }
    }

    public static class ViewPagerHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;

        private final ViewPager viewPager;

        ViewPagerHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_group);
            viewPager = itemView.findViewById(R.id.view_pager);
        }

        public void bind() {
            final int margin = 120;
            List<GroupItem> popularItemList = new ArrayList<>();
            GroupPagerAdapter groupPagerAdapter = new GroupPagerAdapter(popularItemList);

            viewPager.setAdapter(groupPagerAdapter);
            viewPager.setClipToPadding(false);
            viewPager.setPadding(margin, 0, margin, 0);
            viewPager.setPageTransformer(false, (page, pos) -> {
                if (viewPager.getCurrentItem() == 0) {
                    page.setTranslationX(-(margin * 3) / 4);
                } else if (viewPager.getCurrentItem() == groupPagerAdapter.getCount() - 1) {
                    page.setTranslationX(margin * 3 / 4);
                } else {
                    page.setTranslationX(-((margin / 2) + (margin / 8)));
                }
            });
            viewPager.setPageMargin(margin / 4);
            progressBar.setVisibility(View.VISIBLE);
            AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
                Source source = new Source(response);
                List<Element> list = source.getAllElements("id", "accordion", false);

                list.forEach(element -> {
                    try {
                        Element menuList = element.getFirstElementByClass("menu_list");

                        if (element.getAttributeValue("class").equals("accordion")) {
                            int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                            String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                            String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                            StringBuilder info = new StringBuilder();
                            String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                            String joinType = menuList.getAllElementsByClass("info").get(1).getTextExtractor().toString().trim();

                            element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info").forEach(span -> {
                                String extractedText = span.getTextExtractor().toString();

                                info.append(extractedText.contains("회원수") ?
                                        extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                        extractedText + "\n");
                            });
                            GroupItem groupItem = new GroupItem();

                            groupItem.setId(String.valueOf(id));
                            groupItem.setImage(imageUrl);
                            groupItem.setName(name);
                            groupItem.setInfo(info.toString().trim());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            popularItemList.add(groupItem);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
                groupPagerAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }, error -> {
                VolleyLog.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                }

                @Override
                public byte[] getBody() {
                    Map<String, String> params = new HashMap<>();

                    params.put("panel_id", "3");
                    params.put("encoding", "utf-8");
                    if (params.size() > 0) {
                        StringBuilder encodedParams = new StringBuilder();

                        try {
                            params.forEach((k, v) -> {
                                try {
                                    encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                    encodedParams.append('=');
                                    encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                    encodedParams.append('&');
                                } catch (UnsupportedEncodingException uee) {
                                    throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                                }
                            });
                            return encodedParams.toString().getBytes(getParamsEncoding());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    throw new RuntimeException();
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}