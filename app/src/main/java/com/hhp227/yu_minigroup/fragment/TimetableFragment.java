package com.hhp227.yu_minigroup.fragment;


import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimetableFragment extends Fragment {
    private static final String[] TAB_NAMES = {"학기시간표", "모의시간표 작성"};
    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    public TimetableFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        List<Fragment> fragmentList = new ArrayList<>();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), 0) {
            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }
        };
        mActivity = (AppCompatActivity) getActivity();
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mToolbar = rootView.findViewById(R.id.toolbar);
        mTabLayout = rootView.findViewById(R.id.tab_layout);
        mViewPager = rootView.findViewById(R.id.view_pager);
        mActivity.setTitle(getString(R.string.timetable));
        mActivity.setSupportActionBar(mToolbar);
        setDrawerToggle();
        fragmentList.add(SemesterTimeTableFragment.newInstance());
        fragmentList.add(MockTimeTableFragment.newInstance());
        Arrays.stream(TAB_NAMES).forEach(s -> mTabLayout.addTab(mTabLayout.newTab().setText(s)));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(adapter);

        return rootView;
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
