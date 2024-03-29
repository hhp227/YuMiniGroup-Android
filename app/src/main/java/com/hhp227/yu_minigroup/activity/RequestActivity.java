package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.adapter.GroupListAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityListBinding;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;
import com.hhp227.yu_minigroup.viewmodel.RequestViewModel;

public class RequestActivity extends AppCompatActivity {
    private GroupListAdapter mAdapter;

    private ActivityListBinding mBinding;

    private RequestViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        mAdapter = new GroupListAdapter(this);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
        mAdapter.setButtonType(GroupInfoFragment.TYPE_CANCEL);
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (dy > 0 && manager != null && manager.findLastCompletelyVisibleItemPosition() >= manager.getItemCount() - 1) {
                    recyclerView.removeOnScrollListener(this);
                    mViewModel.fetchNextPage();
                    recyclerView.postDelayed(() -> recyclerView.addOnScrollListener(this), 500);
                }
            }
        });
        mBinding.srlList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                }, 1000);
            }
        });
        mViewModel.getState().observe(this, new Observer<RequestViewModel.State>() {
            @Override
            public void onChanged(RequestViewModel.State state) {
                if (state.isLoading) {
                    if (!state.hasRequestedMore) {
                        showProgressBar();
                    } else {
                        mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                    }
                } else if (state.hasRequestedMore) {
                    mViewModel.fetchGroupList(state.offset);
                } else if (!state.groupItemList.isEmpty() || state.isEndReached) {
                    hideProgressBar();
                    mAdapter.submitList(state.groupItemList);
                    mAdapter.setFooterProgressBarVisibility(state.isEndReached ? View.GONE : View.INVISIBLE);
                    mBinding.text.setText("가입신청중인 그룹이 없습니다.");
                    mBinding.rlGroup.setVisibility(mAdapter.getItemCount() > 1 ? View.GONE : View.VISIBLE);
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressBar();
                    Snackbar.make(mBinding.recyclerView, state.message, Snackbar.LENGTH_LONG).show();
                    mAdapter.setFooterProgressBarVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.recyclerView.clearOnScrollListeners();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        mBinding.srlList.setRefreshing(false);
        mViewModel.refresh();
    }

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
        if (!mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.startShimmer();
        if (!mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
        if (mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.stopShimmer();
        if (mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.GONE);
    }
}
