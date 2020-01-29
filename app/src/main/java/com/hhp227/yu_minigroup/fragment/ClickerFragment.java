package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.yu_minigroup.R;

public class ClickerFragment extends Fragment {

    public ClickerFragment() {
    }

    public static ClickerFragment newInstance() {
        ClickerFragment fragment = new ClickerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clicker, container, false);
        return rootView;
    }
}
