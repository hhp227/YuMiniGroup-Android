package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.SeatListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.SeatItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeatFragment extends Fragment {
    public static final String TAG = "도서관 좌석";

    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private List<SeatItem> mSeatItemList;
    private ProgressBar mProgressBar;
    private SeatListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;

    public SeatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seat, container, false);
        CollapsingToolbarLayout toolbarLayout = rootView.findViewById(R.id.collapsing_toolbar);
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_seat);
        mActivity = (AppCompatActivity) getActivity();
        mProgressBar = rootView.findViewById(R.id.pb_seat);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_seat);
        mToolbar = rootView.findViewById(R.id.toolbar);
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mSeatItemList = new ArrayList<>();
        mAdapter = new SeatListAdapter(getActivity(), mSeatItemList);

        mActivity.setTitle(getString(R.string.library_seat));
        mActivity.setSupportActionBar(mToolbar);
        toolbarLayout.setTitleEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(this::refresh, 1000));
        setDrawerToggle();
        showProgressBar();
        fetchDataTask();

        return rootView;
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
        }, error -> VolleyLog.e(error.getMessage())));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void refresh() {
        mSeatItemList.clear();
        fetchDataTask();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void showProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.GONE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
    }
}
