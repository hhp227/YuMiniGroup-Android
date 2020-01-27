package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
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
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<MessageItem> mMessageItems;
    private String mUid;

    public MessageListAdapter(Context context, List<MessageItem> messageItems, String uid) {
        this.mContext = context;
        this.mMessageItems = messageItems;
        this.mUid = uid;
    }

    @Override
    public MessageListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View leftView = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false);
            return new MessageListHolder(leftView);
        } else if (viewType == MSG_TYPE_RIGHT) {
            View rightView = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false);
            return new MessageListHolder(rightView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(MessageListHolder holder, int position) {
        MessageItem messageItem = mMessageItems.get(position);
        holder.name.setText(messageItem.getName());
        holder.message.setText(messageItem.getMessage());
        holder.timeStamp.setText(getTimeStamp(messageItem.getTimeStamp()));
        if (position > 0 && getTimeStamp(mMessageItems.get(position - 1).getTimeStamp()).equals(getTimeStamp(messageItem.getTimeStamp())) && mMessageItems.get(position - 1).getFrom().equals(messageItem.getFrom())) {
            holder.name.setVisibility(View.GONE);
            holder.messageBox.setPadding(holder.messageBox.getPaddingLeft(), 0, holder.messageBox.getPaddingRight(), holder.messageBox.getPaddingBottom());
            holder.profileImage.setVisibility(View.INVISIBLE);
        } else {
            holder.name.setVisibility(getItemViewType(position) == MSG_TYPE_RIGHT ? View.GONE : View.VISIBLE);
            holder.profileImage.setVisibility(View.VISIBLE);
            holder.messageBox.setPadding(10, 10, 10, 10);
            Glide.with(mContext)
                    .load(messageItem.getFrom() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                            .build()) : null)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(holder.profileImage);
        }
        if (position + 1 != mMessageItems.size() && getTimeStamp(messageItem.getTimeStamp()).equals(getTimeStamp(mMessageItems.get(position + 1).getTimeStamp())) && messageItem.getFrom().equals(mMessageItems.get(position + 1).getFrom()))
            holder.timeStamp.setText("");
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

    private String getTimeStamp(long time) {
        return new SimpleDateFormat("a h:mm", Locale.getDefault()).format(time);
    }

    public class MessageListHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private LinearLayout messageBox;
        private TextView name, message, timeStamp;

        public MessageListHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            messageBox = itemView.findViewById(R.id.ll_message);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_message);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
