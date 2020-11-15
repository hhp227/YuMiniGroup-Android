package com.hhp227.yu_minigroup.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WebViewActivity;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.SeatItem;

import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.SeatListHolder> {
    private final List<SeatItem> mSearItemList;

    public SeatListAdapter(List<SeatItem> mSearItemList) {
        this.mSearItemList = mSearItemList;
    }

    @NonNull
    @Override
    public SeatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seat_item, parent, false);
        return new SeatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatListHolder holder, int position) {
        holder.bind(mSearItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSearItemList.size();
    }

    public class SeatListHolder extends RecyclerView.ViewHolder {
        private final ProgressBar rate;

        private final TextView name, seat, status;

        SeatListHolder(View itemView) {
            super(itemView);
            CardView cardView = itemView.findViewById(R.id.card_view);
            name = itemView.findViewById(R.id.name);
            seat = itemView.findViewById(R.id.seat);
            rate = itemView.findViewById(R.id.pb_seat);
            status = itemView.findViewById(R.id.status);

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), WebViewActivity.class);

                intent.putExtra("url", EndPoint.URL_YU_LIBRARY_SEAT_DETAIL.replace("{ID}", mSearItemList.get(getAdapterPosition()).id));
                intent.putExtra("title", itemView.getContext().getString(R.string.library_seat));
                itemView.getContext().startActivity(intent);
            });
        }

        private void bind(SeatItem seatItem) {
            int rateNum = Integer.parseInt(seatItem.percentageInteger);

            name.setText(seatItem.name);
            seat.setText("[" + Integer.parseInt(seatItem.occupied) + "/" + seatItem.count + "]");
            rate.setProgress(rateNum);
            status.setText(seatItem.status);
        }
    }
}
