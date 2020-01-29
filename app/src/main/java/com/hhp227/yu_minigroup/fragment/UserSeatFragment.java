package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.fragment.SemesterTimeTableFragment;

public class UserSeatFragment extends Fragment {

    public UserSeatFragment() {
    }

    public static SemesterTimeTableFragment newInstance() {
        SemesterTimeTableFragment fragment = new SemesterTimeTableFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_seat, container, false);
        WebView webView = rootView.findViewById(R.id.wv_seat);
        WebSettings webSettings = webView.getSettings();
        webView.loadUrl(EndPoint.URL_YU_LIBRARY_SEAT);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
        return rootView;
    }
}
