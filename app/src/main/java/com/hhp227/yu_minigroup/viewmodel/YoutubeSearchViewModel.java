package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.YouTubeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class YoutubeSearchViewModel extends ListViewModel<YouTubeItem> {
    public static final String API_KEY = "AIzaSyCHF6p97aduruLMxgCuEVfFaKUiGPcMuOQ";

    private static final int LIMIT = 50;

    private final MutableLiveData<String> mQuery = new MutableLiveData<>("");

    public void setQuery(String query) {
        mQuery.postValue(query);
    }

    public LiveData<String> getQuery() {
        return mQuery;
    }

    public void refresh() {
        setQuery(mQuery.getValue());
    }

    public void requestData(String query) {
        Executors.newSingleThreadExecutor().execute(() -> fetchDataTask(query));
    }

    private void fetchDataTask(String query) {
        setLoading(true);
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YOUTUBE_API + "?part=snippet&key=" + API_KEY + "&q=" + query + "&maxResults=" + LIMIT, null, response -> {
            try {
                List<YouTubeItem> youTubeItems = new ArrayList<>();
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

                    youTubeItems.add(youTubeItem);
                }
                setLoading(false);
                setItemList(youTubeItems);
            } catch (JSONException e) {
                setLoading(false);
                setMessage(e.getMessage());
            }
        }, error -> {
            setLoading(false);
            setMessage(error.getMessage());
        }));
    }
}