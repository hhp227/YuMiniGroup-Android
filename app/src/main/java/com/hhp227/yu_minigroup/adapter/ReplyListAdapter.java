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
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.ReplyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplyListAdapter extends BaseAdapter {
    public final List<Map.Entry<String, ReplyItem>> mReplyItemList;

    public ReplyListAdapter(List<Map.Entry<String, ReplyItem>> replyItemList) {
        this.mReplyItemList = replyItemList;
    }

    @Override
    public int getCount() {
        return mReplyItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mReplyItemList.get(position);
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
        viewHolder.bind(mReplyItemList.get(position).getValue());
        return convertView;
    }

    public void submitList(List<Map.Entry<String, ReplyItem>> replyItemList) {
        mReplyItemList.clear();
        mReplyItemList.addAll(replyItemList);
        notifyDataSetChanged();
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