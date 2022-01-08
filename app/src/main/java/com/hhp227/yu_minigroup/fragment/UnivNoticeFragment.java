package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.BbsListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.BbsItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class UnivNoticeFragment extends Fragment {
    private static final int MAX_PAGE = 10; // 최대볼수 있는 페이지 수

    private static final String TAG = "영대소식";

    private boolean mHasRequestedMore; // 데이터 불러올때 중복안되게 하기위한 변수

    private int mOffSet;

    private AppCompatActivity mActivity;

    private ArrayList<BbsItem> mBbsItemArrayList;

    private BbsListAdapter mAdapter;

    private DrawerLayout mDrawerLayout;

    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Toolbar mToolbar;

    public UnivNoticeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mActivity = (AppCompatActivity) getActivity();
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mProgressBar = view.findViewById(R.id.progress_circular);
        mToolbar = view.findViewById(R.id.toolbar);
        mSwipeRefreshLayout = view.findViewById(R.id.srl);
        mBbsItemArrayList = new ArrayList<>();
        mAdapter = new BbsListAdapter(mBbsItemArrayList);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mHasRequestedMore && !recyclerView.canScrollVertically(1)) {
                    if (mOffSet != MAX_PAGE) {
                        mHasRequestedMore = true;
                        mOffSet++; // offSet 증가
                        fetchDataList();
                        Snackbar.make(recyclerView, "게시판 정보 불러오는 중...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else
                        mHasRequestedMore = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        // 처음 offSet은 1이다, 파싱이 되는 동안 업데이트 될것
        mOffSet = 1;

        mActivity.setTitle(getString(R.string.yu_news));
        mActivity.setSupportActionBar(mToolbar);
        setDrawerToggle();
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mOffSet = 1; // offSet 초기화

            mBbsItemArrayList.clear();
            mSwipeRefreshLayout.setRefreshing(false);
            fetchDataList();
        }, 1000));
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        showProgressBar();
        fetchDataList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOnScrollListener != null)
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fetchDataList() {
        String tag_string_req = "req_yu_news";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_YU_NOTICE.replace("{PAGE}", String.valueOf(mOffSet)), this::onResponse, this::onErrorResponse);

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void onResponse(String response) {
        Source source = new Source(response);

        try {
            Element boardList = source.getFirstElementByClass("boardList");

            for (Element tr : boardList.getFirstElement(HTMLElementName.TBODY).getAllElements(HTMLElementName.TR)) {
                BbsItem bbsItem = new BbsItem();
                List<Element> tds = tr.getChildElements();
                String id = tds.get(0).getContent().toString();
                String title = tds.get(1).getTextExtractor().toString();
                String writer = tds.get(2).getContent().toString();
                String date = tds.get(3).getContent().toString();

                bbsItem.setId(id);
                bbsItem.setTitle(title);
                bbsItem.setWriter(writer);
                bbsItem.setDate(date);
                mBbsItemArrayList.add(bbsItem);
            }
            mAdapter.notifyDataSetChanged();
            mHasRequestedMore = false;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            hideProgressBar();
        }
    }

    private void onErrorResponse(VolleyError error) {
        if (error.getMessage() != null)
            Log.e(TAG, error.getMessage());
        hideProgressBar();
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
