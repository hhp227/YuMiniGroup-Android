package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WriteActivity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabHostLayoutFragment extends Fragment {
    private static final String IS_ADMIN = "admin";
    private static final String GROUP_ID = "grp_id";
    private static final String GROUP_NAME = "grp_nm";
    private static final String POSITION = "position";
    private static final String KEY = "key";
    private static final String[] TAB_NAMES = {"소식", "일정", "맴버", "설정"};
    private boolean mIsAdmin;
    private int mPosition;
    private String mGroupId, mGroupName, mKey;

    public TabHostLayoutFragment() {
    }

    public static TabHostLayoutFragment newInstance(boolean isAdmin, String groupId, String groupName, int position, String key) {
        TabHostLayoutFragment fragment = new TabHostLayoutFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_ADMIN, isAdmin);
        args.putString(GROUP_ID, groupId);
        args.putString(GROUP_NAME, groupName);
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
            mPosition = getArguments().getInt(POSITION);
            mKey = getArguments().getString(KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_host_layout, container, false);
        CollapsingToolbarLayout toolbarLayout = rootView.findViewById(R.id.collapsing_toolbar);
        FloatingActionButton floatingActionButton = rootView.findViewById(R.id.fab);
        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        List<Fragment> fragmentList = Stream.<Fragment>builder()
                .add(Tab1Fragment.newInstance(mIsAdmin, mGroupId, mGroupName, mKey))
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

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(mGroupName);
        toolbarLayout.setTitleEnabled(false);
        Arrays.stream(TAB_NAMES).forEach(s -> tabLayout.addTab(tabLayout.newTab().setText(s)));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                floatingActionButton.setVisibility(position != 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(TAB_NAMES.length);
        viewPager.setAdapter(adapter);
        floatingActionButton.setOnClickListener(v -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("key", mKey);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
