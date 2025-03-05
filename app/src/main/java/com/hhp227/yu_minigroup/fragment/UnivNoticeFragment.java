package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.WebViewActivity;
import com.hhp227.yu_minigroup.adapter.BbsListAdapter;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentListBinding;
import com.hhp227.yu_minigroup.dto.BbsItem;
import com.hhp227.yu_minigroup.viewmodel.UnivNoticeViewModel;

// TODO
public class UnivNoticeFragment extends Fragment {
    private BbsListAdapter mAdapter;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private FragmentListBinding mBinding;

    private UnivNoticeViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.yu_news));
        mBinding.srl.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mViewModel.refresh();
            mBinding.srl.setRefreshing(false);
        }, 1000));
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((v, p) -> {
            BbsItem bbsItem = mAdapter.getCurrentList().get(p);
            Intent intent = new Intent(getContext(), WebViewActivity.class);

            intent.putExtra("url", EndPoint.URL_YU_NOTICE.replace("{MODE}", "view") + "&articleNo={ARTICLE_NO}".replace("{ARTICLE_NO}", bbsItem.getId()));
            intent.putExtra("title", getString(R.string.yu_news));
            startActivity(intent);
        });
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                if (state.hasRequestedMore) {
                    Snackbar.make(requireView(), "게시판 정보 불러오는 중...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    showProgressBar();
                }
            } else if (state.hasRequestedMore) {
                mViewModel.fetchDataList(state.offset);
            } else if (!state.bbsItems.isEmpty()) {
                hideProgressBar();
                mAdapter.submitList(state.bbsItems);
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
        mBinding = null;
    }

    private void showProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.GONE)
            mBinding.progressCircular.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.VISIBLE)
            mBinding.progressCircular.setVisibility(View.GONE);
    }
}
