package com.hhp227.yu_minigroup.viewmodel;

import android.os.CountDownTimer;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupMainViewModel extends ViewModel {
    private static final String TAG = GroupMainViewModel.class.getSimpleName();

    private final MutableLiveData<Long> mTick = new MutableLiveData<>();

    private final MutableLiveData<State> mState = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CountDownTimer mCountDownTimer = new CountDownTimer(80000, 8000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mTick.postValue(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            start();
        }
    };

    public GroupMainViewModel() {
        fetchDataTask();
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void startCountDownTimer() {
        mCountDownTimer.start();
    }

    public void cancelCountDownTimer() {
        mCountDownTimer.cancel();
    }

    public LiveData<Long> getTick() {
        return mTick;
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mState.postValue(new State(true, Collections.emptyList(), null));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Map.Entry<String, Object>> groupItemList = new ArrayList<>();

            try {
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);

                for (Element elementA : listElementA) {
                    try {
                        String id = groupIdExtract(elementA.getAttributeValue("onclick"));
                        boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                        String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        GroupItem groupItem = new GroupItem();

                        groupItem.setId(id);
                        groupItem.setAdmin(isAdmin);
                        groupItem.setImage(image);
                        groupItem.setName(name);
                        groupItemList.add(new AbstractMap.SimpleEntry<>(id, groupItem));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                initFirebaseData(insertAdvertisement(groupItemList));
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mState.postValue(new State(false, Collections.emptyList(), error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "2");
                params.put("start", "1");
                params.put("display", "10");
                params.put("encoding", "utf-8");
                return params;
            }
        });
    }

    private List<Map.Entry<String, Object>> insertAdvertisement(List<Map.Entry<String, Object>> groupItemList) {
        Map<String, String> headerMap = new HashMap<>();

        if (!groupItemList.isEmpty()) {
            headerMap.put("text", "가입중인 그룹");
            groupItemList.add(0, new AbstractMap.SimpleEntry<>("가입중인 그룹", headerMap));
            if (groupItemList.size() % 2 == 0) {
                groupItemList.add(new AbstractMap.SimpleEntry<>("광고", "광고"));
            }
        } else {
            groupItemList.add(new AbstractMap.SimpleEntry<>("없음", "없음"));
            headerMap.put("text", "인기 모임");
            groupItemList.add(new AbstractMap.SimpleEntry<>("인기 모임", headerMap));
            groupItemList.add(new AbstractMap.SimpleEntry<>("뷰페이져", "뷰페이져"));
        }
        return groupItemList;
    }

    private String groupIdExtract(String href) {
        return href.split("'")[3].trim();
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void initFirebaseData(List<Map.Entry<String, Object>> groupItemList) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(mPreferenceManager.getUser().getUid()).orderByValue().equalTo(true), false, groupItemList);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion, List<Map.Entry<String, Object>> groupItemList) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                        if (groupItem != null) {
                            int index = groupItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(groupItem.getId());

                            if (index > -1) {
                                Map.Entry<String, Object> entry = groupItemList.get(index);

                                groupItemList.set(index, new AbstractMap.SimpleEntry<>(key, entry.getValue()));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        mState.postValue(new State(false, groupItemList, null));
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                            String key = snapshot.getKey();

                            if (key != null) {
                                fetchDataTaskFromFirebase(databaseReference.child(key), true, groupItemList);
                            }
                        }
                    } else {
                        mState.postValue(new State(false, groupItemList, null));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mState.postValue(new State(false, Collections.emptyList(), databaseError.getMessage()));
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, Object>> groupItemList;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, Object>> groupItemList, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.message = message;
        }
    }
}
