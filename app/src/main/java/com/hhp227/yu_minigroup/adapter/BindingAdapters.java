package com.hhp227.yu_minigroup.adapter;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;

public class BindingAdapters {
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
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
}