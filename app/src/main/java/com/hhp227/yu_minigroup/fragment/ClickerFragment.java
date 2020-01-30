package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.SeatListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.SeatItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class ClickerFragment extends Fragment {
    private static final String TAG = "도서관좌석현황";
    private List<SeatItem> mSeatItemList;
    private ProgressBar mProgressBar;
    private SeatListAdapter mAdapter;

    public ClickerFragment() {
    }

    public static ClickerFragment newInstance() {
        ClickerFragment fragment = new ClickerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clicker, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_seat);
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.srl_seat);
        mProgressBar = rootView.findViewById(R.id.pb_seat);
        mSeatItemList = new ArrayList<>();
        mAdapter = new SeatListAdapter(getActivity(), mSeatItemList);

        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        showProgressBar();
        fetchDataTask();

        return rootView;
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.URL_YU_LIBRARY_SEAT, response -> {
            hideProgressBar();
            try {
                Source source = new Source(response);
                Element mainContent = source.getFirstElementByClass("maincontent");
                mainContent.getAllElementsByClass("clicker_libtech_table_list").forEach(element -> {
                    element.getFirstElement(HTMLElementName.TBODY).getAllElements(HTMLElementName.TR).forEach(tr -> {
                        String name = tr.getAllElementsByClass("clicker_align_left").get(0).getContent().toString().trim();
                        String total = tr.getFirstElementByClass("clicker_align_right").getContent().toString().trim();
                        String residual = tr.getFirstElementByClass("clicker_align_right clicker_font_bold").getTextExtractor().toString().trim();
                        String rate = tr.getFirstElementByClass("clicker_progress progress-green").getTextExtractor().toString().trim();
                        String status = tr.getAllElementsByClass("clicker_align_left").get(1).getContent().toString().trim();

                        SeatItem seatItem = new SeatItem(name, total, residual, rate, status);
                        mSeatItemList.add(seatItem);
                        mAdapter.notifyItemInserted(mSeatItemList.size() - 1);
                    });
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }, error -> VolleyLog.e(error.getMessage())));
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
