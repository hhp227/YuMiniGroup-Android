package com.hhp227.yu_minigroup.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.WebViewActivity;
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
        return new SeatListHolder(SeatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final SeatItemBinding mBinding;

        SeatListHolder(SeatItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), WebViewActivity.class);

                intent.putExtra("url", EndPoint.URL_YU_LIBRARY_SEAT_DETAIL.replace("{ID}", mSearItemList.get(getAdapterPosition()).id));
                intent.putExtra("title", itemView.getContext().getString(R.string.library_seat));
                itemView.getContext().startActivity(intent);
            });
        }

        private void bind(SeatItem seatItem) {
            mBinding.name.setText(seatItem.name);
            mBinding.seat.setText("[" + Integer.parseInt(seatItem.occupied) + "/" + seatItem.count + "]");
            mBinding.pbSeat.setProgress(Integer.parseInt(seatItem.percentageInteger));
            mBinding.status.setText(seatItem.status);
        }
    }
}
