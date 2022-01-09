package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

//TODO viewBinding으로 이전할 것

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListHolder> {
    private static final int MSG_TYPE_LEFT = 0;

    private static final int MSG_TYPE_RIGHT = 1;

    private final List<MessageItem> mMessageItems;

    private final String mUid;

    public MessageListAdapter(List<MessageItem> messageItems, String uid) {
        this.mMessageItems = messageItems;
        this.mUid = uid;
    }

    @NonNull
    @Override
    public MessageListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View leftView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_left, parent, false);
            return new MessageListHolder(leftView);
        } else if (viewType == MSG_TYPE_RIGHT) {
            View rightView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_right, parent, false);
            return new MessageListHolder(rightView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(MessageListHolder holder, int position) {
        holder.bind(mMessageItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessageItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return !mMessageItems.get(position).getFrom().equals(mUid) ? MSG_TYPE_LEFT : MSG_TYPE_RIGHT;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static String getTimeStamp(long time) {
        return new SimpleDateFormat("a h:mm", Locale.getDefault()).format(time);
    }

    public class MessageListHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;

        private final LinearLayout messageBox;

        private final TextView name, message, timeStamp;

        public MessageListHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            messageBox = itemView.findViewById(R.id.ll_message);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_message);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
        }

        public void bind(MessageItem messageItem) {
            name.setText(messageItem.getName());
            message.setText(messageItem.getMessage());
            timeStamp.setText(getTimeStamp(messageItem.getTimeStamp()));
            if (getAdapterPosition() > 0 && getTimeStamp(mMessageItems.get(getAdapterPosition() - 1).getTimeStamp()).equals(getTimeStamp(messageItem.getTimeStamp())) && mMessageItems.get(getAdapterPosition() - 1).getFrom().equals(messageItem.getFrom())) {
                name.setVisibility(View.GONE);
                messageBox.setPadding(messageBox.getPaddingLeft(), 0, messageBox.getPaddingRight(), messageBox.getPaddingBottom());
                profileImage.setVisibility(View.INVISIBLE);
            } else {
                name.setVisibility(!mMessageItems.get(getAdapterPosition()).getFrom().equals(mUid) ? MSG_TYPE_LEFT : View.GONE);
                profileImage.setVisibility(View.VISIBLE);
                messageBox.setPadding(messageBox.getPaddingLeft(), 10, messageBox.getPaddingRight(), 10); // 수정완료 경북대 소모임에도 반영할것
                Glide.with(itemView.getContext())
                        .load(messageItem.getFrom() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom()), new LazyHeaders.Builder()
                                .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                                .build()) : null)
                        .apply(RequestOptions
                                .errorOf(R.drawable.user_image_view_circle)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(profileImage);
            }

            // 경북대 소모임에도 반영할것
            timeStamp.setVisibility(getAdapterPosition() + 1 != mMessageItems.size() && getTimeStamp(messageItem.getTimeStamp()).equals(getTimeStamp(mMessageItems.get(getAdapterPosition() + 1).getTimeStamp())) && messageItem.getFrom().equals(mMessageItems.get(getAdapterPosition() + 1).getFrom()) ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
