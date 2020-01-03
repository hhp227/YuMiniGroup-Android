package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.CreateActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;

import java.util.HashMap;
import java.util.Map;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    private AppCompatActivity activity;
    private Button findGroup, requestGroup, createGroup;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        activity = (AppCompatActivity) getActivity();
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        findGroup = rootView.findViewById(R.id.b_find);
        requestGroup = rootView.findViewById(R.id.b_request);
        createGroup = rootView.findViewById(R.id.b_create);
        toolbar = rootView.findViewById(R.id.toolbar);
        activity.setTitle("Fragment01");
        activity.setSupportActionBar(toolbar);
        setDrawerToggle();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, "http://lms.yu.ac.kr/ilos/community/share_group_member_list.acl?CLUB_GRP_ID=91", response -> {
            Toast.makeText(getContext(), response.trim(), Toast.LENGTH_LONG).show();
        }, error -> {
            VolleyLog.e(error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }
        });
        findGroup.setOnClickListener(v -> {
            Toast.makeText(getContext(), "소모임 찾기", Toast.LENGTH_LONG).show();
        });
        requestGroup.setOnClickListener(v -> {
            Toast.makeText(getContext(), "가입신청중인 그룹", Toast.LENGTH_LONG).show();
        });
        createGroup.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
        });

        return rootView;
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
