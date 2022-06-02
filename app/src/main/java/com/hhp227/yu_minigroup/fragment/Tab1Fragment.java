package com.hhp227.yu_minigroup.fragment;

import static com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel.mGroupId;
import static com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel.mGroupImage;
import static com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel.mGroupName;
import static com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel.mIsAdmin;
import static com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel.mKey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.ArticleActivity;
import com.hhp227.yu_minigroup.activity.CreateArticleActivity;
import com.hhp227.yu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.databinding.FragmentTab1Binding;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.viewmodel.Tab1ViewModel;

public class Tab1Fragment extends Fragment {
    private long mLastClickTime;

    private ArticleListAdapter mAdapter;

    private FragmentTab1Binding mBinding;

    private ActivityResultLauncher<Intent> mArticleActivityResultLauncher;

    private Tab1ViewModel mViewModel;

    public static Tab1Fragment newInstance(boolean isAdmin, String grpId, String grpNm, String grpImg, String key) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_nm", grpNm);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab1Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(Tab1ViewModel.class);
        mAdapter = new ArticleListAdapter(mViewModel.mArticleItemKeys, mViewModel.mArticleItemValues);
        mArticleActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    int position = result.getData().getIntExtra("position", 0) - 1;
                    ArticleItem articleItem = mViewModel.mArticleItemValues.get(position);

                    articleItem.setTitle(result.getData().getStringExtra("sbjt"));
                    articleItem.setContent(result.getData().getStringExtra("txt"));
                    articleItem.setImages(result.getData().getStringArrayListExtra("img")); // firebase data
                    articleItem.setReplyCount(result.getData().getStringExtra("cmmt_cnt"));
                    articleItem.setYoutube(result.getData().getParcelableExtra("youtube"));
                    mViewModel.mArticleItemValues.set(position, articleItem);
                    mAdapter.notifyItemChanged(position);
                } else {
                    mViewModel.refresh();
                    mBinding.rvArticle.scrollToPosition(0);
                    ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
                }
            }
        });

        mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
        mBinding.rvArticle.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvArticle.setAdapter(mAdapter);
        mBinding.rvArticle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (dy > 0 && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
                    mViewModel.fetchNextPage();
                }
            }
        });
        mAdapter.setOnItemClickListener((v, position) -> {
            ArticleItem articleItem = mViewModel.mArticleItemValues.get(position);
            Intent intent = new Intent(getContext(), ArticleActivity.class);

            intent.putExtra("admin", mIsAdmin);
            intent.putExtra("grp_id", mGroupId);
            intent.putExtra("grp_nm", mGroupName);
            intent.putExtra("grp_img", mGroupImage);
            intent.putExtra("artl_num", articleItem.getId());
            intent.putExtra("position", position + 1);
            intent.putExtra("auth", articleItem.isAuth() || AppController.getInstance().getPreferenceManager().getUser().getUid().equals(articleItem.getUid()));
            intent.putExtra("isbottom", v.getId() == R.id.ll_reply);
            intent.putExtra("grp_key", mKey);
            intent.putExtra("artl_key", mAdapter.getKey(position));
            mArticleActivityResultLauncher.launch(intent);
        });
        mBinding.rlWrite.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                return;
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(getActivity(), CreateArticleActivity.class);

            intent.putExtra("grp_id", mGroupId);
            intent.putExtra("grp_nm", mGroupName);
            intent.putExtra("grp_img", mGroupImage);
            intent.putExtra("grp_key", mKey);
            intent.putExtra("type", 0);
            ((TabHostLayoutFragment) requireParentFragment()).mCreateArticleResultLauncher.launch(intent);
        });
        mBinding.srlArticleList.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mViewModel.refresh();
            mBinding.srlArticleList.setRefreshing(false);
        }, 2000));
        mBinding.srlArticleList.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                if (!state.hasRequestedMore) {
                    showProgressBar();
                } else {
                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                }
            } else if (state.hasRequestedMore) {
                mViewModel.fetchArticleList(state.offset);
            } else if (!state.articleItemKeys.isEmpty() && !state.articleItemValues.isEmpty()) {
                hideProgressBar();
                mViewModel.addAll(state.articleItemKeys, state.articleItemValues);
                mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
                mBinding.rlWrite.setVisibility(mAdapter.getItemCount() > 1 ? View.GONE : View.VISIBLE);
            } else if (state.isEndReached) {
                hideProgressBar();
                mAdapter.setFooterProgressBarVisibility(View.GONE);
                mBinding.rlWrite.setVisibility(mAdapter.getItemCount() > 1 ? View.GONE : View.VISIBLE);
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                mAdapter.setFooterProgressBarVisibility(View.GONE);
                Snackbar.make(mBinding.rvArticle, state.message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mArticleActivityResultLauncher = null;
    }

    public void onCreateArticleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mViewModel.refresh();
            mBinding.rvArticle.scrollToPosition(0);
            ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
        }
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showProgressBar() {
        if (mBinding.pbArticle.getVisibility() == View.GONE)
            mBinding.pbArticle.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbArticle.getVisibility() == View.VISIBLE)
            mBinding.pbArticle.setVisibility(View.GONE);
    }
}
