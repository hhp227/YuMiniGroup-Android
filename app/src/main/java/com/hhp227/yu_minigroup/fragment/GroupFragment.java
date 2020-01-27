package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    public static final int UPDATE_GROUP = 30;

    private static final String TAG = GroupFragment.class.getSimpleName();
    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private GridLayoutManager mGridLayoutManager;
    private GroupGridAdapter mAdapter;
    private List<String> mGroupItemKeys;
    private List<Object> mGroupItemValues;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bnv_group_button);
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_group);
        mActivity = (AppCompatActivity) getActivity();
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mToolbar = rootView.findViewById(R.id.toolbar);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_group);
        mProgressBar = rootView.findViewById(R.id.pb_group);
        mRelativeLayout = rootView.findViewById(R.id.rl_group);
        mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupGridAdapter(mActivity, mGroupItemKeys, mGroupItemValues);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
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
                intent.putExtra("pos", position);
                intent.putExtra("key", mAdapter.getKey(position));
                startActivityForResult(intent, UPDATE_GROUP);
            }
        });
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT ? 2 : 1;
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
        if (AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();
        showProgressBar();
        fetchDataTask();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE) && resultCode == Activity.RESULT_OK) {
            mGroupItemKeys.clear();
            mGroupItemValues.clear();
            fetchDataTask();
        } else if (requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "ok", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT ? 2 : 1;
                    }
                });
                mGridLayoutManager.setSpanCount(2);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT ? 4 : 1;
                    }
                });
                mGridLayoutManager.setSpanCount(4);
                break;
        }
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> listElementA = source.getAllElements(HTMLElementName.A);
            mAdapter.addHeaderView("가입중인 그룹");
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
        initFirebaseData();
        if (mGroupItemValues.size() % 2 == 0) {
            GroupItem ad = new GroupItem();
            ad.setAd(true);
            ad.setName("광고");
            mGroupItemValues.add(ad);
        }
        hideProgressBar();
        mRelativeLayout.setVisibility(mGroupItemValues.isEmpty() ? View.VISIBLE : View.GONE);
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
                    }
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                        fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                    }
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
