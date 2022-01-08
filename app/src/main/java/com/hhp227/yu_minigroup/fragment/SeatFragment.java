package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.MainActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.SeatListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.yu_minigroup.dto.SeatItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeatFragment extends Fragment {
    public static final String TAG = "도서관 좌석";

    private AppCompatActivity mActivity;

    private List<SeatItem> mSeatItemList;

    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    public SeatFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (AppCompatActivity) getActivity();
        mSeatItemList = new ArrayList<>();
        mAdapter = new SeatListAdapter(mSeatItemList);

        mActivity.setTitle(getString(R.string.library_seat));
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.collapsingToolbar.setTitleEnabled(false);
        mBinding.rvSeat.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvSeat.setAdapter(mAdapter);
        mBinding.srlSeat.setOnRefreshListener(() -> new Handler().postDelayed(this::refresh, 1000));
        setDrawerToggle();
        showProgressBar();
        fetchDataTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YU_LIBRARY_SEAT_ROOMS, null, response -> {
            hideProgressBar();
            try {
                JSONArray jsonArray = response.getJSONArray("_Model_lg_clicker_reading_room_brief_list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("l_id");
                    String roomName = jsonObject.getString("l_room_name");
                    String count = jsonObject.getString("l_count");
                    String occupied = jsonObject.getString("l_occupied");
                    String percentage = jsonObject.getString("l_percentage_integer");
                    String openMode = jsonObject.getString("l_open_mode");
                    SeatItem seatItem = new SeatItem(id, roomName, count, occupied, percentage, openMode);

                    mSeatItemList.add(seatItem);
                    mAdapter.notifyItemChanged(mSeatItemList.size() - 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            if (error.getMessage() != null)
                VolleyLog.e(error.getMessage());
        }));
    }

    private void setDrawerToggle() {
        DrawerLayout drawerLayout = ((MainActivity) mActivity).mBinding.drawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, mBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void refresh() {
        mSeatItemList.clear();
        fetchDataTask();
        mBinding.srlSeat.setRefreshing(false);
    }

    private void showProgressBar() {
        if (mBinding.pbSeat.getVisibility() == View.GONE)
            mBinding.pbSeat.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbSeat.getVisibility() == View.VISIBLE)
            mBinding.pbSeat.setVisibility(View.GONE);
    }
}
