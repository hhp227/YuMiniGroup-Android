package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private static final int NAME_MAX_LINE = 2;
    private Context mContext;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;

    public GroupListAdapter(Context mContext, List<String> mGroupItemKeys, List<GroupItem> mGroupItemValues) {
        this.mContext = mContext;
        this.mGroupItemKeys = mGroupItemKeys;
        this.mGroupItemValues = mGroupItemValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupItem groupItem = mGroupItemValues.get(position);

        holder.groupName.setText(groupItem.getName());
        holder.groupName.setMaxLines(NAME_MAX_LINE);
        holder.groupInfo.setText(groupItem.getJoinType().equals("0") ? "가입방식: 자동 승인" : "가입방식: 운영자 승인 확인");

        Glide.with(mContext).load(groupItem.getImage()).apply(RequestOptions.errorOf(R.drawable.ic_launcher_background)).into(holder.groupImage);
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage;
        private TextView groupName, groupInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_group_name);
            groupInfo = itemView.findViewById(R.id.tv_info);
        }
    }
}
