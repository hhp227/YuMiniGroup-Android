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

import com.hhp227.yu_minigroup.R;

public class GroupFragment extends Fragment {
    private AppCompatActivity activity;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        activity = (AppCompatActivity) getActivity();
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        toolbar = rootView.findViewById(R.id.toolbar);
        activity.setTitle("Fragment01");
        activity.setSupportActionBar(toolbar);
        setDrawerToggle();

        return rootView;
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
