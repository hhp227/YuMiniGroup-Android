package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.databinding.FragmentTab2Binding;
import com.hhp227.yu_minigroup.databinding.HeaderCalendarBinding;
import com.hhp227.yu_minigroup.databinding.ScheduleItemBinding;
import com.hhp227.yu_minigroup.viewmodel.Tab2ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tab2Fragment extends Fragment {
    private CalendarAdapter mAdapter;

    private FragmentTab2Binding mBinding;

    private Tab2ViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab2Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(Tab2ViewModel.class);
        mAdapter = new CalendarAdapter();

        mBinding.rvCal.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvCal.setAdapter(mAdapter);
        mViewModel.getCalendar().observe(getViewLifecycleOwner(), calendar -> {
            mViewModel.fetchDataTask(calendar);
            if (mBinding.rvCal.getChildCount() > 0) {
                ((HeaderHolder) mBinding.rvCal.getChildViewHolder(mBinding.rvCal.getChildAt(0))).mBinding.calendar.setCalendar(calendar);
            }
        });
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                Log.e("TEST", "isLoading");
            } else if (!state.list.isEmpty()) {
                mAdapter.submitList(state.list);
            } else if (state.message != null && !state.message.isEmpty()) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        private final HeaderCalendarBinding mBinding;

        public HeaderHolder(HeaderCalendarBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.calendar.prev.setOnClickListener(v -> mViewModel.previousMonth());
            mBinding.calendar.next.setOnClickListener(v -> mViewModel.nextMonth());
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private final ScheduleItemBinding mBinding;

        public ItemHolder(ScheduleItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(Map<String, String> map) {
            mBinding.date.setText(map.get("날짜"));
            mBinding.content.setText(map.get("내용"));
        }
    }

    private class CalendarAdapter extends RecyclerView.Adapter {
        private static final int TYPE_CALENDAR = 0;

        private static final int TYPE_ITEM = 1;

        private final List<Map<String, String>> mCurrentList = new ArrayList<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_CALENDAR) {
                return new HeaderHolder(HeaderCalendarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else if (viewType == TYPE_ITEM) {
                return new ItemHolder(ScheduleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
            throw new RuntimeException();
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemHolder) {
                ((ItemHolder) holder).bind(mCurrentList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mCurrentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_CALENDAR : TYPE_ITEM;
        }

        public void submitList(List<Map<String, String>> list) {
            mCurrentList.clear();
            mCurrentList.addAll(list);
            notifyDataSetChanged();
        }
    }
}
