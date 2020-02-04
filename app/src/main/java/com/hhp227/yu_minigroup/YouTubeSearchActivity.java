package com.hhp227.yu_minigroup;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.adapter.YouTubeListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class YouTubeSearchActivity extends AppCompatActivity {
    private static final int LIMIT = 50;
    private static final String API_KEY = "AIzaSyA-Y2OLwlPCIjr-CtLG6_CyPQWLAiRi7qg";
    private YouTubeListAdapter mAdapter;
    private List<YouTubeItem> mYouTubeItemList;
    private ProgressBar mProgressBar;
    private String searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.srl_list);
        mProgressBar = findViewById(R.id.pb_group);
        mYouTubeItemList = new ArrayList<>();
        mAdapter = new YouTubeListAdapter(this, mYouTubeItemList);
        searchText = "";

        setSupportActionBar(toolbar);
        mAdapter.setOnItemClickListener((v, position) -> Toast.makeText(getApplicationContext(), "다음버젼에서 첨부 가능합니다.", Toast.LENGTH_LONG).show());
        mAdapter.setHasStableIds(true);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mYouTubeItemList.clear();
            fetchDataTask();
            swipeRefreshLayout.setRefreshing(false);
        }, 1000));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        showProgressBar();
        fetchDataTask();
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
                showProgressBar();
                mYouTubeItemList.clear();
                searchText = query;
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
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YOUTUBE_API + "?part=snippet&key=" + API_KEY + "&q=" + searchText + "&maxResults=" + LIMIT, null, response -> {
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
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            hideProgressBar();
        }));
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
