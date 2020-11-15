package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
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
import com.hhp227.yu_minigroup.dto.ReplyItem;

import java.util.List;

public class ReplyListAdapter extends BaseAdapter {
    private final List<String> mReplyItemKeys;

    private final List<ReplyItem> mReplyItemValues;

    private LayoutInflater mInflater;

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

        if (mInflater == null)
            mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.reply_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        // 댓글 데이터 얻기
        ReplyItem replyItem = mReplyItemValues.get(position);

        Glide.with(convertView.getContext())
                .load(replyItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", replyItem.getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                        .build()) : null)
                .apply(RequestOptions
                        .errorOf(R.drawable.user_image_view_circle)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(viewHolder.profileImage);
        viewHolder.name.setText(replyItem.getName());
        viewHolder.reply.setText(replyItem.getReply());
        viewHolder.timeStamp.setText(replyItem.getDate());
        return convertView;
    }

    public String getKey(int position) {
        return mReplyItemKeys.get(position);
    }

    private static class ViewHolder {
        private final ImageView profileImage;

        private final TextView name, reply, timeStamp;

        public ViewHolder(View itemView) {
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            name = itemView.findViewById(R.id.tv_name);
            reply = itemView.findViewById(R.id.tv_reply);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}