package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.EndPoint;

public class BusFragment extends Fragment {
    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;

    public BusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = view.findViewById(R.id.wv_bus);
        WebSettings webSettings = webView.getSettings();
        mActivity = (AppCompatActivity) getActivity();
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mToolbar = view.findViewById(R.id.toolbar);

        mActivity.setTitle(getString(R.string.shuttle_bus));
        mActivity.setSupportActionBar(mToolbar);
        setDrawerToggle();

        // 임시로 웹뷰 사용
        webView.loadUrl(EndPoint.URL_YU_SHUTTLE_BUS);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
