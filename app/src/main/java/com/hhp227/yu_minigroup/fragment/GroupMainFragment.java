package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.hhp227.yu_minigroup.handler.OnFragmentGroupMainEventListener;
import com.hhp227.yu_minigroup.viewmodel.GroupMainViewModel;

import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.yu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;

public class GroupMainFragment extends Fragment implements OnFragmentGroupMainEventListener {
    private GroupGridAdapter mAdapter;

    private RecyclerView.ItemDecoration mItemDecoration;

    private FragmentGroupMainBinding mBinding;

    private GroupMainViewModel mViewModel;

    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentGroupMainBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(GroupMainViewModel.class);
        mItemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);

                if (parent.getAdapter() != null && parent.getLayoutManager() instanceof GridLayoutManager && position != RecyclerView.NO_POSITION && (parent.getAdapter().getItemViewType(position) == TYPE_GROUP || parent.getAdapter().getItemViewType(position) == TYPE_AD)) {
                    int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
                    outRect.top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    outRect.bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

                    if (position % spanCount == 0) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                    } else if (position % spanCount == 1) {
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
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        mBinding.setSpanCount(getResources().getInteger(R.integer.group_main_span_count));
        mBinding.setOnSpanSizeListener(position -> mAdapter.getItemViewType(position) == TYPE_AD || mAdapter.getItemViewType(position) == TYPE_GROUP ? 1 : mBinding.getSpanCount());
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
        mBinding.rvGroup.setAdapter(mAdapter);
        mBinding.rvGroup.addItemDecoration(mItemDecoration);
        mBinding.srlGroup.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.bnvGroupButton.getMenu().getItem(0).setCheckable(false);
        if (mViewModel.getUser() == null) {
            ((MainActivity) requireActivity()).logout();
        }
        observeViewModelData();
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
        mBinding.setSpanCount(getResources().getInteger(R.integer.group_main_span_count));
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mBinding.srlGroup.setRefreshing(false);
            mViewModel.refresh();
        }, 1700);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), groupItemList -> mAdapter.submitList(groupItemList));
        mViewModel.getPopularItemList().observe(getViewLifecycleOwner(), groupItemList -> mAdapter.submitPopularGroupList(groupItemList));
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
            }
        });
        mViewModel.getTick().observe(getViewLifecycleOwner(), aLong -> mAdapter.moveSliderPager());
    }
}
