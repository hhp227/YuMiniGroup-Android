package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.GroupListAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityListBinding;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;
import com.hhp227.yu_minigroup.handler.OnActivityListEventListener;
import com.hhp227.yu_minigroup.viewmodel.GroupInfoViewModel;
import com.hhp227.yu_minigroup.viewmodel.RequestViewModel;

public class RequestActivity extends AppCompatActivity implements OnActivityListEventListener {
    private GroupListAdapter mAdapter;

    private ActivityListBinding mBinding;

    private RequestViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        mViewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        mAdapter = new GroupListAdapter(this);

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setHandler(this);
        mBinding.setEmptyMessage("가입신청중인 그룹이 없습니다.");
        setAppBar(mBinding.toolbar);
        mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
        mAdapter.setButtonType(GroupInfoViewModel.TYPE_CANCEL);
        mBinding.recyclerView.setHasFixedSize(true);
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
        observeViewModelData();
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

    @Override
    public void onRefresh() {
        new Handler(getMainLooper()).postDelayed(this::refresh, 1000);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(this, groupItemList -> mAdapter.submitList(groupItemList));
        mViewModel.hasRequestMore().observe(this, hasRequestMore -> {
            if (hasRequestMore) {
                mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
            }
        });
        mViewModel.isEndReached().observe(this, isEndReached -> mAdapter.setFooterProgressBarVisibility(isEndReached ? View.GONE : View.INVISIBLE));
        mViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(mBinding.recyclerView, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void refresh() {
        mBinding.srlList.setRefreshing(false);
        mViewModel.refresh();
    }
}
