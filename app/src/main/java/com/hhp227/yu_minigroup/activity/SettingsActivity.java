package com.hhp227.yu_minigroup.activity;

import android.os.Build;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.databinding.ActivitySettingsBinding;
import com.hhp227.yu_minigroup.fragment.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettingsActivity extends AppCompatActivity {
    private static final String[] TAB_NAMES = {"회원관리", "모임정보"};

    private ActivitySettingsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        String groupId = getIntent().getStringExtra("grp_id");
        String groupImage = getIntent().getStringExtra("grp_img");
        String key = getIntent().getStringExtra("key");
        List<Fragment> fragmentList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentList = Stream.<Fragment>builder()
                    .add(MemberManagementFragment.newInstance(groupId))
                    .add(DefaultSettingFragment.newInstance(groupId, groupImage, key))
                    .build()
                    .collect(Collectors.toList());
        } else {
            fragmentList = Arrays.asList(MemberManagementFragment.newInstance(groupId), DefaultSettingFragment.newInstance(groupId, groupImage, key));
        }
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("소모임 설정");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Arrays.stream(TAB_NAMES).forEach(s -> mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s)));
        } else {
            for (String s : TAB_NAMES)
                mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s));
        }
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.viewPager));
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding.tabLayout.clearOnTabSelectedListeners();
        mBinding.tabLayout.removeAllTabs();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}