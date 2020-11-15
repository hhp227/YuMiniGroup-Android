package com.hhp227.yu_minigroup;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.yu_minigroup.fragment.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettingsActivity extends AppCompatActivity {
    private static final String[] TAB_NAMES = {"회원관리", "모임정보"};

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        String groupId = getIntent().getStringExtra("grp_id");
        String groupImage = getIntent().getStringExtra("grp_img");
        String key = getIntent().getStringExtra("key");
        List<Fragment> fragmentList = Stream.<Fragment>builder()
                .add(MemberManagementFragment.newInstance(groupId))
                .add(DefaultSettingFragment.newInstance(groupId, groupImage, key))
                .build()
                .collect(Collectors.toList());
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
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("소모임 설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Arrays.stream(TAB_NAMES).forEach(s -> mTabLayout.addTab(mTabLayout.newTab().setText(s)));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
        mTabLayout.clearOnTabSelectedListeners();
        mTabLayout.removeAllTabs();
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