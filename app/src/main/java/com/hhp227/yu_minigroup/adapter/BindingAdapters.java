package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.ArticleActivity;
import com.hhp227.yu_minigroup.calendar.ExtendedCalendarView;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;
import static com.hhp227.yu_minigroup.viewmodel.YoutubeSearchViewModel.API_KEY;

public class BindingAdapters {
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(view);
        }
    }

    @BindingAdapter(value = {"userImageUrl", "cookie"}, requireAll = false)
    public static void loadUserImage(ImageView view, String url, String cookie) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(new GlideUrl(url, new LazyHeaders.Builder()
                            .addHeader("Cookie", cookie)
                            .build()))
                    .apply(new RequestOptions().circleCrop()
                            .error(R.drawable.user_image_view_circle)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(view);
        }
    }

    @BindingAdapter("userImageBitmap")
    public static void loadUserImage(ImageView view, Bitmap bitmap) {
        if (bitmap != null) {
            Glide.with(view.getContext())
                    .load(bitmap)
                    .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                    .into(view);
        }
    }

    @BindingAdapter("onFocusChange")
    public static void focusChange(View view, View.OnFocusChangeListener onFocusChangeListener) {
        view.setOnFocusChangeListener(onFocusChangeListener);
    }

    @BindingAdapter("onRefresh")
    public static void refresh(SwipeRefreshLayout swipeRefreshLayout, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    @BindingAdapter("onNavigationItemSelected")
    public static void setOnNavigationItemSelectedListener(NavigationBarView view, NavigationBarView.OnItemSelectedListener listener) {
        view.setOnItemSelectedListener(listener);
    }

    @BindingAdapter(value = {"spanCount", "spanSize"}, requireAll = true)
    public static void bindSpanCount(RecyclerView view, int spanCount, Function<Integer, Integer> spanSizeListener) {
        GridLayoutManager layoutManager = (GridLayoutManager) view.getLayoutManager();

        if (layoutManager != null) {
            layoutManager.setSpanCount(spanCount);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (view.getAdapter() != null && position == view.getAdapter().getItemCount())
                            ? spanCount
                            : spanSizeListener.apply(position);
                }
            });
        }
        view.invalidateItemDecorations();
    }


    @BindingAdapter(value = {"verticalArrangement", "horizontalArrangement"}, requireAll = false)
    public static void setItemOffsets(RecyclerView view, float verticalArrangement, float horizontalArrangement) {
        RecyclerView.ItemDecoration oldDecoration = (RecyclerView.ItemDecoration) view.getTag(-1);

        if (oldDecoration != null) {
            view.removeItemDecoration(oldDecoration);
        }

        RecyclerView.ItemDecoration decoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View itemView, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, itemView, parent, state);
                int position = parent.getChildAdapterPosition(itemView);

                if (parent.getAdapter() != null && parent.getLayoutManager() instanceof GridLayoutManager && position != RecyclerView.NO_POSITION) {
                    int itemViewType = parent.getAdapter().getItemViewType(position);

                    if (itemViewType == TYPE_GROUP || itemViewType == TYPE_AD) {
                        int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
                        int vertical = dpToPx(parent, verticalArrangement);
                        int horizontal = dpToPx(parent, horizontalArrangement);
                        outRect.top = vertical;
                        outRect.bottom = vertical / 2;

                        if (position % spanCount == 0) {
                            outRect.left = horizontal / 2;
                            outRect.right = horizontal;
                        } else if (position % spanCount == 1) {
                            outRect.left = horizontal;
                            outRect.right = horizontal / 2;
                        } else {
                            outRect.left = horizontal / 2;
                            outRect.right = horizontal / 2;
                        }
                    }
                }
            }
        };

        view.addItemDecoration(decoration);
        view.setTag(-1, decoration);
    }

    private static int dpToPx(View view, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, view.getResources().getDisplayMetrics());
    }

    @BindingAdapter(value = {"imageList", "onImageClick", "youtube"}, requireAll = false)
    public static void bindImageList(LinearLayout view, List<String> list, Consumer<Integer> onImageClickListener, YouTubeItem youTubeItem) {
        view.removeAllViews();
        if (list != null) {
            view.removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                int finalI = i;
                ImageView articleImage = new ImageView(view.getContext());

                articleImage.setAdjustViewBounds(true);
                articleImage.setPadding(0, 0, 0, 30);
                articleImage.setScaleType(ImageView.ScaleType.FIT_XY);
                articleImage.setOnClickListener(v -> onImageClickListener.accept(finalI));
                Glide.with(view)
                        .load(list.get(i))
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .into(articleImage);
                view.addView(articleImage);
            }
        }
        if (youTubeItem != null) {
            LinearLayout youtubeContainer = new LinearLayout(view.getContext());
            YouTubePlayerView youTubePlayerView = new YouTubePlayerView(view.getContext());

            youTubePlayerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.setShowFullscreenButton(true);
                    if (b) {
                        youTubePlayer.play();
                    } else {
                        try {
                            youTubePlayer.cueVideo(youTubeItem.videoId);
                        } catch (IllegalStateException e) {
                            youTubePlayerView.initialize(API_KEY, this);
                        }
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    try {
                        if (youTubeInitializationResult.isUserRecoverableError())
                            youTubeInitializationResult.getErrorDialog((Activity) view.getContext(), 0).show();
                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e(ArticleActivity.class.getSimpleName(), e.getMessage());
                        }
                    }
                }
            });
            youtubeContainer.addView(youTubePlayerView);
            youtubeContainer.setPadding(0, 0, 0, 30);
            view.addView(youtubeContainer, youTubeItem.position);
        }
    }

    @BindingAdapter("calendar")
    public static void bindCalendar(ExtendedCalendarView view, Calendar calendar) {
        view.setCalendar(calendar);
    }

    @BindingAdapter(value = {"onPrevClick", "onNextClick"}, requireAll = false)
    public static void setOnCalendarClickListener(ExtendedCalendarView view, View.OnClickListener onPrevClickListener, View.OnClickListener onNextClickListener) {
        view.prev.setOnClickListener(onPrevClickListener);
        view.next.setOnClickListener(onNextClickListener);
    }
}