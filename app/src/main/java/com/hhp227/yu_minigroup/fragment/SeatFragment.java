package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.adapter.SeatListAdapter;
import com.hhp227.yu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.yu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.yu_minigroup.viewmodel.SeatViewModel;

public class SeatFragment extends Fragment implements OnFragmentListEventListener {
    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    private SeatViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(SeatViewModel.class);
        mAdapter = new SeatListAdapter();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.library_seat));
        mBinding.rvSeat.setAdapter(mAdapter);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            mViewModel.refresh();
            mBinding.srlSeat.setRefreshing(false);
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), seatItemList -> mAdapter.submitList(seatItemList));
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}