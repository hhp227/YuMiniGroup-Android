package com.hhp227.yu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupGridAdapter extends RecyclerView.Adapter<GroupGridAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;

    public GroupGridAdapter(Context mContext, List<String> mGroupItemKeys, List<GroupItem> mGroupItemValues) {
        this.mContext = mContext;
        this.mGroupItemKeys = mGroupItemKeys;
        this.mGroupItemValues = mGroupItemValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupItem groupItem = mGroupItemValues.get(position);
        holder.groupName.setText(groupItem.getName());
        holder.more.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mContext, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_group, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_group_menu1:
                        Toast.makeText(mContext, "테스트1", Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.action_group_menu2:
                        Toast.makeText(mContext, "테스트2", Toast.LENGTH_LONG).show();
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage, more;
        private LinearLayout groupLayout;
        private TextView groupName;

        public ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.title);
            more = itemView.findViewById(R.id.iv_more);
        }
    }
}
