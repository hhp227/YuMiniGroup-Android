package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
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
import com.hhp227.yu_minigroup.*;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    private AppCompatActivity mActivity;
    private DrawerLayout drawerLayout;
    private GroupGridAdapter mGroupGridAdapter;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private PreferenceManager mPreferenceManager;
    private RecyclerView myGroupList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        Button findGroup = rootView.findViewById(R.id.b_find);
        Button requestGroup = rootView.findViewById(R.id.b_request);
        Button createGroup = rootView.findViewById(R.id.b_create);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        mActivity = (AppCompatActivity) getActivity();
        drawerLayout = mActivity.findViewById(R.id.drawer_layout);
        myGroupList = rootView.findViewById(R.id.rv_group);
        toolbar = rootView.findViewById(R.id.toolbar);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_group);
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mGroupGridAdapter = new GroupGridAdapter(getContext(), mGroupItemKeys, mGroupItemValues);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mActivity.setTitle("메인화면");
        mActivity.setSupportActionBar(toolbar);
        setDrawerToggle();
        myGroupList.setLayoutManager(gridLayoutManager);
        myGroupList.setAdapter(mGroupGridAdapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                mGroupItemKeys.clear();
                mGroupItemValues.clear();
                fetchDataTask();
                swipeRefreshLayout.setRefreshing(false);
            }, 1700);
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        findGroup.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), FindActivity.class), REGISTER_CODE);
        });
        requestGroup.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), RequestActivity.class));
        });
        createGroup.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
        });
        if (AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();

        for (int i = 0; i < 8; i++) {
            GroupItem groupItem = new GroupItem();
            groupItem.setName("테스트");
            mGroupItemValues.add(groupItem);
            mGroupGridAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, "http://lms.yu.ac.kr/ilos/community/share_group_member_list.acl?CLUB_GRP_ID=91", response -> {
            Toast.makeText(getContext(), response.trim(), Toast.LENGTH_LONG).show();
        }, error -> {
            VolleyLog.e(error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }
        });
    }

    private void logout() {
        mPreferenceManager.clear();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }
}
