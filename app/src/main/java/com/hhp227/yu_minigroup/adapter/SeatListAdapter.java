package com.hhp227.yu_minigroup.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WebViewActivity;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.SeatItem;

import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter {
    private Activity mActivity;
    private List<SeatItem> mSearItemList;

    public SeatListAdapter(Activity mActivity, List<SeatItem> mSearItemList) {
        this.mActivity = mActivity;
        this.mSearItemList = mSearItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.seat_item, parent, false);
        return new SeatListHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SeatListHolder) {
            SeatItem seatItem = mSearItemList.get(position);
            int rateNum = Integer.parseInt(seatItem.percentageInteger);
            ((SeatListHolder) holder).name.setText(seatItem.name);
            ((SeatListHolder) holder).seat.setText("[" + Integer.parseInt(seatItem.occupied) + "/" + seatItem.count + "]");
            ((SeatListHolder) holder).rate.setProgress(rateNum);
            ((SeatListHolder) holder).status.setText(seatItem.status);
            ((SeatListHolder) holder).cardView.setOnClickListener(v -> {
                Intent intent = new Intent(mActivity, WebViewActivity.class);
                intent.putExtra("url", EndPoint.URL_YU_LIBRARY_SEAT_DETAIL.replace("{ID}", seatItem.id));
                intent.putExtra("title", mActivity.getString(R.string.library_seat));
                mActivity.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSearItemList.size();
    }

    public static class SeatListHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ProgressBar rate;
        private TextView name, seat, status;

        SeatListHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            name = itemView.findViewById(R.id.name);
            seat = itemView.findViewById(R.id.seat);
            rate = itemView.findViewById(R.id.pb_seat);
            status = itemView.findViewById(R.id.status);
        }
    }
}
