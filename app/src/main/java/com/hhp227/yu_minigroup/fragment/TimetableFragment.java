package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimetableFragment extends Fragment {
    public static final String TAG = "시간표";

    private static final String[] TAB_NAMES = {"학기시간표", "모의시간표 작성"};

    private AppCompatActivity mActivity;

    private FragmentTabsBinding mBinding;

    public TimetableFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTabsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Fragment> fragmentList = new ArrayList<>();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }
        };
        mActivity = (AppCompatActivity) getActivity();

        mActivity.setTitle(getString(R.string.timetable));
        mActivity.setSupportActionBar(mBinding.toolbar);
        setDrawerToggle();
        fragmentList.add(SemesterTimeTableFragment.newInstance());
        fragmentList.add(MockTimeTableFragment.newInstance());
        Arrays.stream(TAB_NAMES).forEach(s -> mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s)));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.viewPager));
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding.tabLayout.clearOnTabSelectedListeners();
        mBinding.tabLayout.removeAllTabs();
        mBinding = null;
    }

    private void setDrawerToggle() {
        DrawerLayout drawerLayout = ((MainActivity) mActivity).mBinding.drawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, mBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
