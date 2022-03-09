package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentPagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.CreateArticleActivity;
import com.hhp227.yu_minigroup.databinding.FragmentTabHostLayoutBinding;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabHostLayoutFragment extends Fragment {
    private static final String IS_ADMIN = "admin";
    private static final String GROUP_ID = "grp_id";
    private static final String GROUP_NAME = "grp_nm";
    private static final String GROUP_IMAGE = "grp_img";
    private static final String POSITION = "pos";
    private static final String KEY = "key";
    private static final String[] TAB_NAMES = {"소식", "일정", "맴버", "설정"};

    private boolean mIsAdmin;

    private int mPosition;

    private String mGroupId, mGroupName, mGroupImage, mKey;

    private FragmentTabHostLayoutBinding mBinding;

    public TabHostLayoutFragment() {
    }

    public static TabHostLayoutFragment newInstance(boolean isAdmin, String groupId, String groupName, String groupImage, int position, String key) {
        TabHostLayoutFragment fragment = new TabHostLayoutFragment();
        Bundle args = new Bundle();

        args.putBoolean(IS_ADMIN, isAdmin);
        args.putString(GROUP_ID, groupId);
        args.putString(GROUP_NAME, groupName);
        args.putString(GROUP_IMAGE, groupImage);
        args.putInt(POSITION, position);
        args.putString(KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsAdmin = getArguments().getBoolean(IS_ADMIN);
            mGroupId = getArguments().getString(GROUP_ID);
            mGroupName = getArguments().getString(GROUP_NAME);
            mGroupImage = getArguments().getString(GROUP_IMAGE);
            mPosition = getArguments().getInt(POSITION);
            mKey = getArguments().getString(KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTabHostLayoutBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        List<Fragment> fragmentList = Stream.<Fragment>builder()
                .add(Tab1Fragment.newInstance(mIsAdmin, mGroupId, mGroupName, mGroupImage, mKey))
                .add(new Tab2Fragment())
                .add(Tab3Fragment.newInstance(mGroupId))
                .add(Tab4Fragment.newInstance(mIsAdmin, mGroupId, mGroupImage, mPosition, mKey))
                .build()
                .collect(Collectors.toList());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };

        if (activity != null) {
            activity.setSupportActionBar(mBinding.toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setTitle(mGroupName);
            }
        }
        mBinding.collapsingToolbar.setTitleEnabled(false);
        Arrays.stream(TAB_NAMES).forEach(s -> mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s)));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                mBinding.viewPager.setCurrentItem(position);
                mBinding.fab.setVisibility(position != 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setOffscreenPageLimit(TAB_NAMES.length);
        mBinding.viewPager.setAdapter(adapter);
        mBinding.fab.setOnClickListener(v -> {
            if (mBinding.tabLayout.getSelectedTabPosition() == 0) {
                Intent intent = new Intent(getActivity(), CreateArticleActivity.class);

                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("key", mKey);
                startActivity(intent);
            }
        });

        // 경북대 소모임에는 없음
        if (!mGroupImage.contains("share_nophoto")) {
            Glide.with(this)
                    .load(mGroupImage)
                    .apply(RequestOptions.errorOf(R.drawable.header))
                    .into(mBinding.ivHeader);
            mBinding.ivTitle.setVisibility(View.INVISIBLE);
            mBinding.gradient.setVisibility(View.VISIBLE);
            if (activity != null && activity.getWindow() != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
            ViewGroup.LayoutParams toolbarLayoutParams = mBinding.toolbar.getLayoutParams();
            toolbarLayoutParams.height = toolbarLayoutParams.height + getStatusBarHeight();

            mBinding.toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            mBinding.toolbar.setLayoutParams(toolbarLayoutParams);
            LinearLayout.LayoutParams toolbarLayoutLayoutParams = (LinearLayout.LayoutParams) mBinding.collapsingToolbar.getLayoutParams();
            toolbarLayoutLayoutParams.height = toolbarLayoutLayoutParams.height + getStatusBarHeight();

            mBinding.collapsingToolbar.setLayoutParams(toolbarLayoutLayoutParams);
        } else {
            mBinding.ivTitle.setVisibility(View.VISIBLE);
            mBinding.gradient.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding.tabLayout.clearOnTabSelectedListeners();
        mBinding.tabLayout.removeAllTabs();
        mBinding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getChildFragmentManager().getFragments())
            fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:

                // 툴바, 탭레이아웃 간격 벌어짐 귀찮아서 나중에...
                break;
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
