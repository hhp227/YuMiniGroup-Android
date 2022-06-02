package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public class MessageListLeftHolder extends RecyclerView.ViewHolder {
        private final MessageItemLeftBinding mBinding;

        public MessageListLeftHolder(MessageItemLeftBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem) {
            mBinding.tvName.setText(messageItem.getName());
            mBinding.tvMessage.setText(messageItem.getMessage());
            mBinding.tvTimestamp.setText(getTimeStamp(messageItem.getTimestamp()));
            if (getAdapterPosition() > 0 && getTimeStamp(mMessageItems.get(getAdapterPosition() - 1).getTimestamp()).equals(getTimeStamp(messageItem.getTimestamp())) && mMessageItems.get(getAdapterPosition() - 1).getFrom().equals(messageItem.getFrom())) {
                mBinding.tvName.setVisibility(View.GONE);
                mBinding.llMessage.setPadding(mBinding.llMessage.getPaddingLeft(), 0, mBinding.llMessage.getPaddingRight(), mBinding.llMessage.getPaddingBottom());
                mBinding.ivProfileImage.setVisibility(View.INVISIBLE);
            } else {
                //name.setVisibility(!mMessageItems.get(getAdapterPosition()).getFrom().equals(mUid) ? MSG_TYPE_LEFT : View.GONE); 기존코드인데 이상하게 적용되어있다. 확인해볼것
                mBinding.tvName.setVisibility(getItemViewType() == MSG_TYPE_RIGHT ? View.GONE : View.VISIBLE);
                mBinding.ivProfileImage.setVisibility(View.VISIBLE);
                mBinding.llMessage.setPadding(mBinding.llMessage.getPaddingLeft(), 10, mBinding.llMessage.getPaddingRight(), 10); // 수정완료 경북대 소모임에도 반영할것
                Glide.with(itemView.getContext())
                        .load(messageItem.getFrom() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom()), new LazyHeaders.Builder()
                                .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                                .build()) : null)
                        .apply(RequestOptions
                                .errorOf(R.drawable.user_image_view_circle)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(mBinding.ivProfileImage);
            }

            // 경북대 소모임에도 반영할것
            mBinding.tvTimestamp.setVisibility(getAdapterPosition() + 1 != mMessageItems.size() && getTimeStamp(messageItem.getTimestamp()).equals(getTimeStamp(mMessageItems.get(getAdapterPosition() + 1).getTimestamp())) && messageItem.getFrom().equals(mMessageItems.get(getAdapterPosition() + 1).getFrom()) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    public class MessageListRightHolder extends RecyclerView.ViewHolder {
        private final MessageItemRightBinding mBinding;

        public MessageListRightHolder(MessageItemRightBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem) {
            mBinding.tvName.setText(messageItem.getName());
            mBinding.tvMessage.setText(messageItem.getMessage());
            mBinding.tvTimestamp.setText(getTimeStamp(messageItem.getTimestamp()));
            if (getAdapterPosition() > 0 && getTimeStamp(mMessageItems.get(getAdapterPosition() - 1).getTimestamp()).equals(getTimeStamp(messageItem.getTimestamp())) && mMessageItems.get(getAdapterPosition() - 1).getFrom().equals(messageItem.getFrom())) {
                mBinding.tvName.setVisibility(View.GONE);
                mBinding.llMessage.setPadding(mBinding.llMessage.getPaddingLeft(), 0, mBinding.llMessage.getPaddingRight(), mBinding.llMessage.getPaddingBottom());
                mBinding.ivProfileImage.setVisibility(View.INVISIBLE);
            } else {
                //name.setVisibility(!mMessageItems.get(getAdapterPosition()).getFrom().equals(mUid) ? MSG_TYPE_LEFT : View.GONE); 기존코드인데 이상하게 적용되어있다. 확인해볼것
                mBinding.tvName.setVisibility(getItemViewType() == MSG_TYPE_RIGHT ? View.GONE : View.VISIBLE);
                mBinding.ivProfileImage.setVisibility(View.VISIBLE);
                mBinding.llMessage.setPadding(mBinding.llMessage.getPaddingLeft(), 10, mBinding.llMessage.getPaddingRight(), 10); // 수정완료 경북대 소모임에도 반영할것
                Glide.with(itemView.getContext())
                        .load(messageItem.getFrom() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom()), new LazyHeaders.Builder()
                                .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                                .build()) : null)
                        .apply(RequestOptions
                                .errorOf(R.drawable.user_image_view_circle)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(mBinding.ivProfileImage);
            }

            // 경북대 소모임에도 반영할것
            mBinding.tvTimestamp.setVisibility(getAdapterPosition() + 1 != mMessageItems.size() && getTimeStamp(messageItem.getTimestamp()).equals(getTimeStamp(mMessageItems.get(getAdapterPosition() + 1).getTimestamp())) && messageItem.getFrom().equals(mMessageItems.get(getAdapterPosition() + 1).getFrom()) ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
