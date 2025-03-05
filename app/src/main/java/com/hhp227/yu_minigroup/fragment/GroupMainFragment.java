package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.CreateGroupActivity;
import com.hhp227.yu_minigroup.activity.FindGroupActivity;
import com.hhp227.yu_minigroup.activity.GroupActivity;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.RequestActivity;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.databinding.FragmentGroupMainBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.viewmodel.GroupMainViewModel;

import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;

// TODO
public class GroupMainFragment extends Fragment {
    private static final int PORTAIT_SPAN_COUNT = 2;

    private static final int LANDSCAPE_SPAN_COUNT = 4;

    private int mSpanCount;

    private GridLayoutManager mGridLayoutManager;

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;

    private GroupGridAdapter mAdapter;

    private RecyclerView.ItemDecoration mItemDecoration;

    private FragmentGroupMainBinding mBinding;

    private GroupMainViewModel mViewModel;

    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentGroupMainBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GroupMainViewModel.class);
        mSpanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTAIT_SPAN_COUNT :
                     getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? LANDSCAPE_SPAN_COUNT :
                     0;
        mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_BANNER
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_VIEW_PAGER ? mSpanCount : 1;
            }
        };
        mGridLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mItemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getAdapter() != null && parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_GROUP || parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_AD) {
                    outRect.top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    outRect.bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                    if (parent.getChildAdapterPosition(view) % mSpanCount == 0) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                    } else if (parent.getChildAdapterPosition(view) % mSpanCount == 1) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    } else {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    }
                }
            }
        };
        mAdapter = new GroupGridAdapter();
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                mViewModel.refresh();
                ((MainActivity) requireActivity()).updateProfileImage();
            }
        });

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.main));
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener((v, position) -> {
            if (mAdapter.getCurrentList().get(position).getValue() instanceof GroupItem) {
                GroupItem groupItem = (GroupItem) mAdapter.getCurrentList().get(position).getValue();
                Intent intent = new Intent(getContext(), GroupActivity.class);

                intent.putExtra("admin", groupItem.isAdmin());
                intent.putExtra("grp_id", groupItem.getId());
                intent.putExtra("grp_nm", groupItem.getName());
                intent.putExtra("grp_img", groupItem.getImage()); // 경북대 소모임에는 없음
                intent.putExtra("key", mAdapter.getKey(position));
                mActivityResultLauncher.launch(intent);
            }
        });
        mAdapter.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.b_find:
                    mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                    return;
                case R.id.b_create:
                    mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
            }
        });
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mBinding.rvGroup.setLayoutManager(mGridLayoutManager);
        mBinding.rvGroup.setAdapter(mAdapter);
        mBinding.rvGroup.addItemDecoration(mItemDecoration);
        mBinding.srlGroup.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mBinding.srlGroup.setRefreshing(false);
            mViewModel.refresh();
        }, 1700));
        mBinding.srlGroup.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.bnvGroupButton.getMenu().getItem(0).setCheckable(false);
        mBinding.bnvGroupButton.setOnItemSelectedListener(item -> {
            item.setCheckable(false);
            switch (item.getItemId()) {
                case R.id.navigation_find:
                    mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                    return true;
                case R.id.navigation_request:
                    startActivity(new Intent(getContext(), RequestActivity.class));
                    return true;
                case R.id.navigation_create:
                    mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
                    return true;
            }
            return false;
        });
        if (mViewModel.getUser() == null) {
            ((MainActivity) requireActivity()).logout();
        }
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                showProgressBar();
            } else if (!state.groupItemList.isEmpty()) {
                hideProgressBar();
                mAdapter.submitList(state.groupItemList);
                if (getActivity() != null) {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            } else if (!state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show();
                if (getActivity() != null) {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
        mViewModel.getTick().observe(getViewLifecycleOwner(), aLong -> mAdapter.moveSliderPager());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.rvGroup.removeItemDecoration(mItemDecoration);
        mBinding = null;
        mActivityResultLauncher = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.startCountDownTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.cancelCountDownTimer();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mSpanCount = PORTAIT_SPAN_COUNT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mSpanCount = LANDSCAPE_SPAN_COUNT;
                break;
        }
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mGridLayoutManager.setSpanCount(mSpanCount);
        mBinding.rvGroup.invalidateItemDecorations();
    }

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
    }
}
