package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentBusBinding;

public class BusFragment extends Fragment {
    private FragmentBusBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentBusBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebSettings webSettings = mBinding.wvBus.getSettings();

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.shuttle_bus));

        // 임시로 웹뷰 사용
        mBinding.wvBus.loadUrl(EndPoint.URL_YU_SHUTTLE_BUS);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
    }
}
