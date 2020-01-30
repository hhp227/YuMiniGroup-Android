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

public class SeatFragment extends Fragment {
    public static final String TAG = "도서관 좌석";

    private static final String[] TAB_NAMES = {"좌석이용현황", "상세좌석현황"};
    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    public SeatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        List<Fragment> fragmentList = new ArrayList<>();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
        fragmentList.add(ClickerFragment.newInstance());
        fragmentList.add(UserSeatFragment.newInstance());
        Arrays.stream(TAB_NAMES).forEach(s -> mTabLayout.addTab(mTabLayout.newTab().setText(s)));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabLayout.clearOnTabSelectedListeners();
        mViewPager.clearOnPageChangeListeners();
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
