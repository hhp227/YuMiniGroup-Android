package com.hhp227.yu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.MemberListItemBinding;
import com.hhp227.yu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberListAdapter extends BaseAdapter {
    private final List<MemberItem> mMemberItems;

    public MemberListAdapter(List<MemberItem> memberItems) {
        this.mMemberItems = memberItems;
    }

    @Override
    public int getCount() {
        return mMemberItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            MemberListItemBinding binding = MemberListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            viewHolder = new ViewHolder(binding);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.bind(mMemberItems.get(position));
        return convertView;
    }

    public void submitList(List<MemberItem> memberItems) {
        mMemberItems.clear();
        mMemberItems.addAll(memberItems);
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        private final MemberListItemBinding mBinding;

        public ViewHolder(MemberListItemBinding binding) {
            this.mBinding = binding;
        }

        public void bind(MemberItem memberItem) {
            Glide.with(mBinding.getRoot().getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                            .build()))
                    .apply(RequestOptions.circleCropTransform()
                            .error(R.drawable.user_image_view_circle))
                    .into(mBinding.ivProfileImage);
            mBinding.column1.setText(memberItem.name);
            mBinding.column2.setText(memberItem.dept);
            mBinding.column3.setText(memberItem.div);
            mBinding.column4.setText(memberItem.regDate);
        }
    }
}
