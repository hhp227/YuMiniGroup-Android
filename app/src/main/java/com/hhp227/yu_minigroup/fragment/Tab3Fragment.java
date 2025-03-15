package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.yu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.yu_minigroup.databinding.FragmentTab3Binding;
import com.hhp227.yu_minigroup.dto.MemberItem;
import com.hhp227.yu_minigroup.handler.OnFragmentTab3EventListener;
import com.hhp227.yu_minigroup.viewmodel.Tab3ViewModel;

public class Tab3Fragment extends Fragment implements OnFragmentTab3EventListener {
    private MemberGridAdapter mAdapter;

    private FragmentTab3Binding mBinding;

    private Tab3ViewModel mViewModel;

    public static Tab3Fragment newInstance(String grpId) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab3Binding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(Tab3ViewModel.class);
        mAdapter = new MemberGridAdapter();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener((v, position) -> {
            MemberItem memberItem = mAdapter.getCurrentList().get(position);
            String uid = memberItem.uid;
            String name = memberItem.name;
            String value = memberItem.value;
            Bundle args = new Bundle();
            UserDialogFragment newFragment = UserDialogFragment.newInstance();

            args.putString("uid", uid);
            args.putString("name", name);
            args.putString("value", value);
            newFragment.setArguments(args);
            newFragment.show(getChildFragmentManager(), "dialog");
        });
        mBinding.rvMember.setAdapter(mAdapter);
        mBinding.rvMember.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Handler handler = new Handler(Looper.getMainLooper());

                if (!recyclerView.canScrollVertically(1)) {
                    recyclerView.removeOnScrollListener(this);
                    mViewModel.fetchNextPage();
                    handler.postDelayed(() -> recyclerView.addOnScrollListener(this), 1000);
                }
            }
        });
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
            mViewModel.refresh();
            mBinding.srlMember.setRefreshing(false);
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), articleItemList -> mAdapter.submitList(articleItemList));
        mViewModel.hasRequestMore().observe(getViewLifecycleOwner(), hasRequestMore -> {
            if (hasRequestMore) {
                mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
            }
        });
        mViewModel.isEndReached().observe(getViewLifecycleOwner(), isEndReached -> mAdapter.setFooterProgressBarVisibility(isEndReached ? View.GONE : View.INVISIBLE));
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
