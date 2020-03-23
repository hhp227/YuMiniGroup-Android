package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends RecyclerView.Adapter<MemberGridAdapter.MemberGridHolder> {
    private Activity mActivity;
    private List<MemberItem> mMemberItemList;
    private OnItemClickListener mOnItemClickListener;

    public MemberGridAdapter(Activity activity, List<MemberItem> memberItemList) {
        this.mActivity = activity;
        this.mMemberItemList = memberItemList;
    }

    @Override
    public MemberGridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.member_item, parent, false);
        return new MemberGridHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberGridHolder holder, int position) {
        MemberItem memberItem = mMemberItemList.get(position);
        holder.name.setText(memberItem.name);
        Glide.with(mActivity)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                        .build()))
                .apply(new RequestOptions().centerCrop()
                        .error(R.drawable.user_image_view)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(holder.profileImage);
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return mMemberItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public static class MemberGridHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView name;

        public MemberGridHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            name = itemView.findViewById(R.id.tv_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
