package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.adapter.GroupListAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityListBinding;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;
import com.hhp227.yu_minigroup.viewmodel.FindGroupViewModel;

public class FindGroupActivity extends AppCompatActivity {
    private GroupListAdapter mAdapter;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private ActivityListBinding mBinding;

    private FindGroupViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(FindGroupViewModel.class);
        mAdapter = new GroupListAdapter(this, mViewModel.mGroupItemKeys, mViewModel.mGroupItemValues);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (dy > 0 && manager != null && manager.findLastCompletelyVisibleItemPosition() >= manager.getItemCount() - 1) {
                    mViewModel.fetchNextPage();
                }
            }
        };

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.post(() -> {
            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
            mAdapter.setButtonType(GroupInfoFragment.TYPE_REQUEST);
        });
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.srlList.setOnRefreshListener(() -> new Handler(getMainLooper()).postDelayed(() -> {
            mBinding.srlList.setRefreshing(false);
            mViewModel.refresh();
        }, 1000));
        mViewModel.mState.observe(this, state -> {
            if (state.isLoading) {
                if (!state.hasRequestedMore) {
                    showProgressBar();
                } else {
                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                    mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                }
            } else if (state.hasRequestedMore) {
                mViewModel.fetchGroupList(state.offset);
            } else if (state.isSuccess) {
                hideProgressBar();
                mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
                mBinding.text.setText("가입신청중인 그룹이 없습니다.");
                mBinding.rlGroup.setVisibility(mViewModel.mGroupItemValues.size() > 1 ? View.GONE : View.VISIBLE);
            } else if (state.message != null && !state.message.isEmpty()) {
                Snackbar.make(mBinding.recyclerView, state.message, Snackbar.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
        mBinding.sflGroup.clearAnimation();
        mBinding.sflGroup.removeAllViews();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
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
