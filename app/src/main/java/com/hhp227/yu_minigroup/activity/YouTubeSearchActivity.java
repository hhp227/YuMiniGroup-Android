package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.YouTubeListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityListBinding;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class YouTubeSearchActivity extends AppCompatActivity {
    public static final String API_KEY = "AIzaSyCHF6p97aduruLMxgCuEVfFaKUiGPcMuOQ";

    private static final int LIMIT = 50;

    private int mType;

    private YouTubeListAdapter mAdapter;

    private List<YouTubeItem> mYouTubeItemList;

    private String mSearchText;

    private ActivityListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mYouTubeItemList = new ArrayList<>();
        mAdapter = new YouTubeListAdapter(mYouTubeItemList);
        mSearchText = "";
        mType = getIntent().getIntExtra("type", 0);

        setSupportActionBar(mBinding.toolbar);
        mAdapter.setOnItemClickListener((v, position) -> {//리팩토링 요망
            YouTubeItem youTubeItem = mYouTubeItemList.get(position);
            Intent intent = new Intent(this, mType == 0 ? WriteActivity.class : ModifyActivity.class);

            intent.putExtra("youtube", youTubeItem);
            setResult(RESULT_OK, intent);
            finish();
        });//
        mAdapter.setHasStableIds(true);
        mBinding.srlList.setOnRefreshListener(() -> new Handler(getMainLooper()).postDelayed(() -> {
            mYouTubeItemList.clear();
            mAdapter.notifyDataSetChanged();
            fetchDataTask();
            mBinding.srlList.setRefreshing(false);
        }, 1000));
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        showProgressBar();
        fetchDataTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.sflGroup.clearAnimation();
        mBinding.sflGroup.removeAllViews();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
        searchView.setQueryHint("검색어를 입력하세요.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchText = query;

                showProgressBar();
                mBinding.sflGroup.setVisibility(View.VISIBLE);
                mYouTubeItemList.clear();
                mAdapter.notifyDataSetChanged();
                fetchDataTask();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YOUTUBE_API + "?part=snippet&key=" + API_KEY + "&q=" + mSearchText + "&maxResults=" + LIMIT, null, response -> {
            hideProgressBar();
            try {
                JSONArray items = response.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject jsonObject = items.getJSONObject(i);
                    String id = jsonObject.getJSONObject("id").getString("videoId");
                    JSONObject snippet = jsonObject.getJSONObject("snippet");
                    String publishedAt = snippet.getString("publishedAt");
                    String title = snippet.getString("title");
                    String thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                    String channelTitle = snippet.getString("channelTitle");
                    YouTubeItem youTubeItem = new YouTubeItem(id, publishedAt, title, thumbnail, channelTitle);

                    mYouTubeItemList.add(youTubeItem);
                    mAdapter.notifyItemInserted(mYouTubeItemList.size() - 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            hideProgressBar();
        }));
    }

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
        if (!mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.startShimmer();
        if (!mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
        if (mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.stopShimmer();
        if (mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.GONE);
    }
}
