package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.SeatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeatViewModel extends ViewModel {
    private final MutableLiveData<State> mState = new MutableLiveData<>();

    public SeatViewModel() {
        fetchDataTask(false);
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        fetchDataTask(true);
    }

    private void fetchDataTask(boolean isRefresh) {
        mState.postValue(new State(!isRefresh, Collections.emptyList(), null));
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YU_LIBRARY_SEAT_ROOMS, null, response -> {
            try {
                List<SeatItem> seatItemList = new ArrayList<>();
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

                    seatItemList.add(seatItem);
                }
                mState.postValue(new State(false, seatItemList, null));
            } catch (JSONException e) {
                e.printStackTrace();
                mState.postValue(new State(false, Collections.emptyList(), e.getMessage()));
            }
        }, error -> mState.postValue(new State(false, Collections.emptyList(), error.getMessage()))));
    }

    public static final class State {
        public boolean isLoading;

        public List<SeatItem> seatItemList;

        public String message;

        public State(boolean isLoading, List<SeatItem> seatItemList, String message) {
            this.isLoading = isLoading;
            this.seatItemList = seatItemList;
            this.message = message;
        }
    }
}
