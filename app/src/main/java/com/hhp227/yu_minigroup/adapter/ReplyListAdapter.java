package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ReplyItemBinding;
import com.hhp227.yu_minigroup.dto.ReplyItem;

import java.util.List;

public class ReplyListAdapter extends BaseAdapter {
    private final List<String> mReplyItemKeys;

    private final List<ReplyItem> mReplyItemValues;

    public ReplyListAdapter(List<String> replyItemKeys, List<ReplyItem> replyItemValues) {
        this.mReplyItemKeys = replyItemKeys;
        this.mReplyItemValues = replyItemValues;
    }

    @Override
    public int getCount() {
        return mReplyItemValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mReplyItemValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            ReplyItemBinding binding = ReplyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            viewHolder = new ViewHolder(binding);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.bind(mReplyItemValues.get(position));
        return convertView;
    }

    public String getKey(int position) {
        return mReplyItemKeys.get(position);
    }

    private static class ViewHolder {
        private final ReplyItemBinding mBinding;

        public ViewHolder(ReplyItemBinding binding) {
            this.mBinding = binding;
        }

        public void bind(ReplyItem replyItem) {
            Glide.with(mBinding.getRoot().getContext())
                    .load(replyItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", replyItem.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                            .build()) : null)
                    .apply(RequestOptions
                            .errorOf(R.drawable.user_image_view_circle)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mBinding.ivProfileImage);
            mBinding.tvName.setText(replyItem.getName());
            mBinding.tvReply.setText(replyItem.getReply());
            mBinding.tvTimestamp.setText(replyItem.getDate());
        }
    }
}