package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.MessageItemLeftBinding;
import com.hhp227.yu_minigroup.databinding.MessageItemRightBinding;
import com.hhp227.yu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;

    private static final int MSG_TYPE_RIGHT = 1;

    private final List<MessageItem> mMessageItems;

    private final String mUid;

    public MessageListAdapter(List<MessageItem> messageItems, String uid) {
        this.mMessageItems = messageItems;
        this.mUid = uid;
    }

    public void submitList(List<MessageItem> messageItems) {
        mMessageItems.clear();
        if (messageItems != null) {
            mMessageItems.addAll(messageItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            return new MessageListLeftHolder(MessageItemLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == MSG_TYPE_RIGHT) {
            return new MessageListRightHolder(MessageItemRightBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageListLeftHolder) {
            ((MessageListLeftHolder) holder).bind(mMessageItems.get(position));
        } else if (holder instanceof MessageListRightHolder) {
            ((MessageListRightHolder) holder).bind(mMessageItems.get(position));
        }
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

    /*이전 메시지와 같은 시간, 같은 사람의 메시지로 묶이지 않으면 true(이름, 프로필 이미지 표시)*/
    private boolean isSeparated(int position, MessageItem messageItem) {
        return !(position > 0
                && getTimeStamp(mMessageItems.get(position - 1).getTimestamp()).equals(getTimeStamp(messageItem.getTimestamp()))
                && mMessageItems.get(position - 1).getFrom().equals(messageItem.getFrom()));
    }

    /*다음 메시지와 같은 시간, 같은 사람의 메시지면 마지막 메시지에만 시간을 표시*/
    private boolean isTimestampVisible(int position, MessageItem messageItem) {
        return position + 1 == mMessageItems.size()
                || !getTimeStamp(messageItem.getTimestamp()).equals(getTimeStamp(mMessageItems.get(position + 1).getTimestamp()))
                || !messageItem.getFrom().equals(mMessageItems.get(position + 1).getFrom());
    }

    public class MessageListLeftHolder extends RecyclerView.ViewHolder {
        private final MessageItemLeftBinding mBinding;

        public MessageListLeftHolder(MessageItemLeftBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem) {
            mBinding.setMessageItem(messageItem);
            mBinding.setTimeStamp(getTimeStamp(messageItem.getTimestamp()));
            mBinding.setSeparated(isSeparated(getAdapterPosition(), messageItem));
            mBinding.setTimestampVisible(isTimestampVisible(getAdapterPosition(), messageItem));
            mBinding.setCookie(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
            mBinding.executePendingBindings();
        }
    }

    public class MessageListRightHolder extends RecyclerView.ViewHolder {
        private final MessageItemRightBinding mBinding;

        public MessageListRightHolder(MessageItemRightBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem) {
            mBinding.setMessageItem(messageItem);
            mBinding.setTimeStamp(getTimeStamp(messageItem.getTimestamp()));
            mBinding.setSeparated(isSeparated(getAdapterPosition(), messageItem));
            mBinding.setTimestampVisible(isTimestampVisible(getAdapterPosition(), messageItem));
            mBinding.setCookie(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
            mBinding.executePendingBindings();
        }
    }
}
