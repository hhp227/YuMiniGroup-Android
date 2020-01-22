package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends RecyclerView.Adapter<MemberGridAdapter.MemberGridViewHolder> {
    private Activity mActivity;
    private List<MemberItem> mMemberItemList;

    public MemberGridAdapter(Activity mActivity, List<MemberItem> mMemberItemList) {
        this.mActivity = mActivity;
        this.mMemberItemList = mMemberItemList;
    }

    @Override
    public MemberGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.member_item, parent, false);
        return new MemberGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberGridViewHolder holder, int position) {
        MemberItem memberItem = mMemberItemList.get(position);
        holder.name.setText(memberItem.name);
        Glide.with(mActivity)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                        .build()))
                .apply(new RequestOptions().centerCrop().error(R.drawable.profile_img_sqare))
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return mMemberItemList.size();
    }

    public static class MemberGridViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView name;

        public MemberGridViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            name = itemView.findViewById(R.id.tv_name);
        }
    }
}
