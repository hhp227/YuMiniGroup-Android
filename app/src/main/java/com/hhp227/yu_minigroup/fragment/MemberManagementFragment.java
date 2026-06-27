package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import com.hhp227.yu_minigroup.adapter.MemberListAdapter;
import com.hhp227.yu_minigroup.databinding.FragmentMemberBinding;
import com.hhp227.yu_minigroup.handler.OnFragmentMemberManagementEventListener;
import com.hhp227.yu_minigroup.viewmodel.MemberManagementViewModel;

import java.util.ArrayList;

public class MemberManagementFragment extends Fragment implements OnFragmentMemberManagementEventListener {
    private static final String GROUP_ID = "grp_id";

    private MemberListAdapter mAdapter;

    private FragmentMemberBinding mBinding;

    private MemberManagementViewModel mViewModel;

    public MemberManagementFragment() {
    }

    public static MemberManagementFragment newInstance(String grpId) {
        MemberManagementFragment fragment = new MemberManagementFragment();
        Bundle args = new Bundle();

        args.putString(GROUP_ID, grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMemberBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(MemberManagementViewModel.class);
        mAdapter = new MemberListAdapter(new ArrayList<>());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        mBinding.lvMember.setAdapter(mAdapter);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(getContext(), "새로고침", Toast.LENGTH_LONG).show();
            mBinding.srlMember.setRefreshing(false);
        }, 1500);
    }

    private void observeViewModelData() {
        mViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> mBinding.srlMember.setRefreshing(Boolean.TRUE.equals(isLoading)));
        mViewModel.getItemList().observe(getViewLifecycleOwner(), memberItemList -> mAdapter.submitList(memberItemList));
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

