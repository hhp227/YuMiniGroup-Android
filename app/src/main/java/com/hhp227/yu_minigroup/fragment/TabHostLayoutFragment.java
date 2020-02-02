package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WriteActivity;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabHostLayoutFragment extends Fragment {
    private static final String IS_ADMIN = "admin";
    private static final String GROUP_ID = "grp_id";
    private static final String GROUP_NAME = "grp_nm";
    private static final String GROUP_IMAGE = "grp_img";
    private static final String POSITION = "position";
    private static final String KEY = "key";
    private static final String[] TAB_NAMES = {"소식", "일정", "맴버", "설정"};
    private boolean mIsAdmin;
    private int mPosition;
    private String mGroupId, mGroupName, mGroupImage, mKey;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_host_layout, container, false);
        CollapsingToolbarLayout toolbarLayout = rootView.findViewById(R.id.collapsing_toolbar);
        FloatingActionButton floatingActionButton = rootView.findViewById(R.id.fab);
        ImageView headerImage = rootView.findViewById(R.id.iv_header);
        ImageView titleImage = rootView.findViewById(R.id.iv_title);
        View gradient = rootView.findViewById(R.id.gradient);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        List<Fragment> fragmentList = Stream.<Fragment>builder()
                .add(Tab1Fragment.newInstance(mIsAdmin, mGroupId, mGroupName, mGroupImage, mKey))
                .add(new Tab2Fragment())
                .add(Tab3Fragment.newInstance(mGroupId))
                .add(Tab4Fragment.newInstance(mIsAdmin, mGroupId, mPosition, mKey))
                .build()
                .collect(Collectors.toList());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };
        mTabLayout = rootView.findViewById(R.id.tab_layout);
        mToolbar = rootView.findViewById(R.id.toolbar);
        mViewPager = rootView.findViewById(R.id.view_pager);

        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(mGroupName);
        toolbarLayout.setTitleEnabled(false);
        Arrays.stream(TAB_NAMES).forEach(s -> mTabLayout.addTab(mTabLayout.newTab().setText(s)));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mViewPager.setCurrentItem(position);
                floatingActionButton.setVisibility(position != 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setOffscreenPageLimit(TAB_NAMES.length);
        mViewPager.setAdapter(adapter);
        floatingActionButton.setOnClickListener(v -> {
            if (mTabLayout.getSelectedTabPosition() == 0) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);
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
                    .into(headerImage);
            titleImage.setVisibility(View.INVISIBLE);
            gradient.setVisibility(View.VISIBLE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            ViewGroup.LayoutParams toolbarLayoutParams = mToolbar.getLayoutParams();
            toolbarLayoutParams.height = toolbarLayoutParams.height + getStatusBarHeight();
            mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            mToolbar.setLayoutParams(toolbarLayoutParams);
            LinearLayout.LayoutParams toolbarLayoutLayoutParams = (LinearLayout.LayoutParams) toolbarLayout.getLayoutParams();
            toolbarLayoutLayoutParams.height = toolbarLayoutLayoutParams.height + getStatusBarHeight();
            toolbarLayout.setLayoutParams(toolbarLayoutLayoutParams);
        } else {
            titleImage.setVisibility(View.VISIBLE);
            gradient.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
        mTabLayout.clearOnTabSelectedListeners();
        mTabLayout.removeAllTabs();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:

                // 툴바, 탭레이아웃 간격 벌어짐 귀찮아서 나중에...
                break;
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
