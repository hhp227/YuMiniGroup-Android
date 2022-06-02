package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.adapter.SeatListAdapter;
import com.hhp227.yu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.yu_minigroup.viewmodel.SeatViewModel;

public class SeatFragment extends Fragment {
    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    private SeatViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SeatViewModel.class);
        mAdapter = new SeatListAdapter();

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.library_seat));
        mBinding.collapsingToolbar.setTitleEnabled(false);
        mBinding.rvSeat.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvSeat.setAdapter(mAdapter);
        mBinding.srlSeat.setOnRefreshListener(() -> new Handler().postDelayed(this::refresh, 1000));
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                showProgressBar();
            } else if (!state.seatItemList.isEmpty()) {
                hideProgressBar();
                mAdapter.submitList(state.seatItemList);
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void refresh() {
        mViewModel.refresh();
        mBinding.srlSeat.setRefreshing(false);
    }

    private void showProgressBar() {
        if (mBinding.pbSeat.getVisibility() == View.GONE)
            mBinding.pbSeat.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbSeat.getVisibility() == View.VISIBLE)
            mBinding.pbSeat.setVisibility(View.GONE);
    }
}
