package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WebViewActivity;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.BbsItem;

import java.util.List;

public class BbsListAdapter extends RecyclerView.Adapter<BbsListAdapter.BbsListHolder> {
    private Activity mActivity;
    private List<BbsItem> mBbsItemList;

    public BbsListAdapter(Activity activity, List<BbsItem> bbsItemList) {
        this.mActivity = activity;
        this.mBbsItemList = bbsItemList;
    }

    @Override
    public BbsListAdapter.BbsListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.bbs_item, parent, false);
        return new BbsListHolder(view);
    }

    @Override
    public void onBindViewHolder(BbsListAdapter.BbsListHolder holder, int position) {
        BbsItem bbsItem = mBbsItemList.get(position);
        holder.title.setText(bbsItem.getTitle());
        holder.writer.setText(bbsItem.getWriter());
        holder.date.setText(bbsItem.getDate());
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, WebViewActivity.class);
            intent.putExtra("url", EndPoint.URL_YU_MOBILE_NOTICE.replace("{ID}", bbsItem.getId()));
            mActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mBbsItemList.size();
    }

    public static class BbsListHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView title, writer, date;

        public BbsListHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            title = itemView.findViewById(R.id.item_title);
            writer = itemView.findViewById(R.id.item_writer);
            date = itemView.findViewById(R.id.item_date);
        }
    }
}
