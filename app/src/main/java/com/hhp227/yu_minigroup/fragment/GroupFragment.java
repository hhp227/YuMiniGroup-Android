package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.*;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.adapter.LoopPagerAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.ui.CirclePageIndicator;
import com.hhp227.yu_minigroup.helper.ui.LoopViewPager;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    public static final int UPDATE_GROUP = 30;

    private static final int PORTAIT_SPAN_COUNT = 2;
    private static final int LANDSCAPE_SPAN_COUNT = 4;
    private static final String TAG = GroupFragment.class.getSimpleName();
    private int mSpanCount;
    private AppCompatActivity mActivity;
    private CirclePageIndicator mCirclePageIndicator;
    private DrawerLayout mDrawerLayout;
    private GridLayoutManager mGridLayoutManager;
    private GroupGridAdapter mAdapter;
    private List<String> mGroupItemKeys;
    private List<Object> mGroupItemValues;
    private LoopViewPager mLoopViewPager;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;

    private LoopPagerAdapter mLoopPagerAdapter;
    private CountDownTimer mCountDownTimer;

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bnv_group_button);
        RecyclerView recyclerView = view.findViewById(R.id.rv_group);
        mActivity = (AppCompatActivity) getActivity();
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mCirclePageIndicator = view.findViewById(R.id.cpi_theme_slider_indicator);
        mToolbar = view.findViewById(R.id.toolbar);
        mSwipeRefreshLayout = view.findViewById(R.id.srl_group);
        mProgressBar = view.findViewById(R.id.pb_group);
        mRelativeLayout = view.findViewById(R.id.rl_group);
        mLoopViewPager = view.findViewById(R.id.lvp_theme_slider_pager);
        mSpanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTAIT_SPAN_COUNT :
                     getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? LANDSCAPE_SPAN_COUNT :
                     0;
        mGridLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupGridAdapter(mActivity, mGroupItemKeys, mGroupItemValues);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mLoopPagerAdapter = new LoopPagerAdapter(getActivity(), Stream.<String>builder().add("이미지2").add("메인").add("이미지1").build().collect(Collectors.toList()));
        mCountDownTimer = new CountDownTimer(80000, 8000) {
            @Override
            public void onTick(long millisUntilFinished) {
                moveSliderPager();
            }

            @Override
            public void onFinish() {
                start();
            }
        };

        mActivity.setTitle(getString(R.string.main));
        mActivity.setSupportActionBar(mToolbar);
        setDrawerToggle();
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener((v, position) -> {
            GroupItem groupItem = (GroupItem) mGroupItemValues.get(position);
            if (groupItem.isAd())
                Toast.makeText(getContext(), "광고", Toast.LENGTH_LONG).show();
            else {
                Intent intent = new Intent(getContext(), GroupActivity.class);

                intent.putExtra("admin", groupItem.isAdmin());
                intent.putExtra("grp_id", groupItem.getId());
                intent.putExtra("grp_nm", groupItem.getName());
                intent.putExtra("grp_img", groupItem.getImage()); // 경북대 소모임에는 없음
                intent.putExtra("pos", position);
                intent.putExtra("key", mAdapter.getKey(position));
                startActivityForResult(intent, UPDATE_GROUP);
            }
        });
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT ? mSpanCount : 1;
            }
        });
        recyclerView.setLayoutManager(mGridLayoutManager);
        recyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mGroupItemKeys.clear();
            mGroupItemValues.clear();
            mSwipeRefreshLayout.setRefreshing(false);
            fetchDataTask();
        }, 1700));
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            item.setCheckable(false);
            switch (item.getItemId()) {
                case R.id.navigation_find:
                    startActivityForResult(new Intent(getContext(), FindActivity.class), REGISTER_CODE);
                    return true;
                case R.id.navigation_request:
                    startActivity(new Intent(getContext(), RequestActivity.class));
                    return true;
                case R.id.navigation_create:
                    startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
                    return true;
            }
            return false;
        });
        mLoopPagerAdapter.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.b_find:
                    startActivityForResult(new Intent(getContext(), FindActivity.class), REGISTER_CODE);
                    return;
                case R.id.b_create:
                    startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
            }
        });
        mLoopViewPager.setAdapter(mLoopPagerAdapter);
        //mCirclePageIndicator.setViewPager(mLoopViewPager);
        if (AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();
        showProgressBar();
        fetchDataTask();

    }

    @Override
    public void onResume() {
        super.onResume();
        mCountDownTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        CountDownTimer countDownTimer = mCountDownTimer;
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE) && resultCode == Activity.RESULT_OK) {
            mGroupItemKeys.clear();
            mGroupItemValues.clear();
            fetchDataTask();
        } else if (requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK && data != null) {//
            int position = data.getIntExtra("position", 0);
            if (mGroupItemValues.get(position) instanceof GroupItem) {
                GroupItem groupItem = (GroupItem) mGroupItemValues.get(position);

                groupItem.setName(data.getStringExtra("grp_nm"));
                groupItem.setDescription(data.getStringExtra("grp_desc"));
                groupItem.setJoinType(data.getStringExtra("join_div"));
                mGroupItemValues.set(position, groupItem);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mSpanCount = PORTAIT_SPAN_COUNT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mSpanCount = LANDSCAPE_SPAN_COUNT;
                break;
        }
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT ? mSpanCount : 1;
            }
        });
        mGridLayoutManager.setSpanCount(mSpanCount);
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fetchDataTask() {
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            try {
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);
                for (Element elementA : listElementA) {
                    try {
                        String id = groupIdExtract(elementA.getAttributeValue("onclick"));
                        boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                        String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        GroupItem groupItem = new GroupItem();

                        groupItem.setId(id);
                        groupItem.setAdmin(isAdmin);
                        groupItem.setImage(image);
                        groupItem.setName(name);
                        mGroupItemKeys.add(id);
                        mGroupItemValues.add(groupItem);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter.notifyDataSetChanged();
                insertAdvertisement();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                initFirebaseData();
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "2");
                params.put("start", "1");
                params.put("display", "10");
                params.put("encoding", "utf-8");
                return params;
            }
        });
    }

    private void logout() {
        mPreferenceManager.clear();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void insertAdvertisement() {
        if (!mGroupItemValues.isEmpty()) {
            mAdapter.addHeaderView("가입중인 그룹");
            mRelativeLayout.setVisibility(View.GONE);
            if (mGroupItemValues.size() % 2 == 0) {
                GroupItem ad = new GroupItem();

                ad.setAd(true);
                ad.setName("광고");
                mGroupItemValues.add(ad);
            }
        } else {
            mRelativeLayout.setVisibility(View.VISIBLE);
            setMainPageView();
        }
        hideProgressBar();
    }

    private boolean moveSliderPager() {
        if (mLoopViewPager == null || mLoopPagerAdapter.getCount() <= 0) {
            return false;
        }

        LoopViewPager loopViewPager = mLoopViewPager;
        loopViewPager.setCurrentItem(loopViewPager.getCurrentItem() + 1);
        return true;
    }

    private void setMainPageView() {


    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(mPreferenceManager.getUser().getUid()).orderByValue().equalTo(true), false);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);
                        assert value != null;
                        int index = mGroupItemKeys.indexOf(value.getId());
                        if (index > -1) {
                            //mGroupItemValues.set(index, value); //isAdmin값때문에 주석처리
                            mGroupItemKeys.set(index, key);
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        if (getActivity() != null)
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                            fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                        }
                    } else
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String href) {
        return href.split("'")[3].trim();
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void showProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }
}
