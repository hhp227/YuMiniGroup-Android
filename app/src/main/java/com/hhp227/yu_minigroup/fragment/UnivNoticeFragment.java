package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.WebViewActivity;
import com.hhp227.yu_minigroup.adapter.BbsListAdapter;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentListBinding;
import com.hhp227.yu_minigroup.dto.BbsItem;
import com.hhp227.yu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.yu_minigroup.viewmodel.UnivNoticeViewModel;

public class UnivNoticeFragment extends Fragment implements OnFragmentListEventListener {
    private BbsListAdapter mAdapter;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private FragmentListBinding mBinding;

    private UnivNoticeViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentListBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(UnivNoticeViewModel.class);
        mAdapter = new BbsListAdapter();
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                    mViewModel.fetchNextPage();
                }
            }
        };
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.yu_news));
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((v, p) -> {
            BbsItem bbsItem = mAdapter.getCurrentList().get(p);
            Intent intent = new Intent(getContext(), WebViewActivity.class);

            intent.putExtra("url", EndPoint.URL_YU_NOTICE.replace("{MODE}", "view") + "&articleNo={ARTICLE_NO}".replace("{ARTICLE_NO}", bbsItem.getId()));
            intent.putExtra("title", getString(R.string.yu_news));
            startActivity(intent);
        });
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
        mBinding = null;
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mViewModel.refresh();
            mBinding.srl.setRefreshing(false);
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), bbsItemList -> mAdapter.submitList(bbsItemList));
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(mBinding.recyclerView, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}