package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.yu_minigroup.R;

public class DefaultSettingFragment extends Fragment {

    public DefaultSettingFragment() {
    }

    public static DefaultSettingFragment newInstance(String grpId, String grpImg, String key) {
        DefaultSettingFragment fragment = new DefaultSettingFragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_default_setting, container, false);
    }

}
