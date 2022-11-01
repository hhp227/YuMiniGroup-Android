package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
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

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, Collections.emptyList(), 1, false, false, null));

    private static final int LIMIT = 100;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private boolean mStopRequestMore = false;

    private int mMinId;

    public RequestViewModel() {
        fetchNextPage();
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void fetchGroupList(int offset) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> list = source.getAllElements("id", "accordion", false);
            List<Map.Entry<String, GroupItem>> groupItemList = new ArrayList<>();

            for (Element element : list) {
                try {
                    Element menuList = element.getFirstElementByClass("menu_list");

                    if (element.getAttributeValue("class").equals("accordion")) {
                        int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                        String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        StringBuilder info = new StringBuilder();
                        String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                        String joinType = menuList.getAllElementsByClass("info").get(1).getContent().toString();
                        GroupItem groupItem = new GroupItem();
                        mMinId = mMinId == 0 ? id : Math.min(mMinId, id);

                        for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                            String extractedText = span.getTextExtractor().toString();

                            info.append(extractedText.contains("회원수") ?
                                    extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                    extractedText + "\n");
                        }
                        if (id > mMinId) {
                            mStopRequestMore = true;
                            break;
                        } else
                            mStopRequestMore = false;
                        groupItem.setId(String.valueOf(id));
                        groupItem.setImage(imageUrl);
                        groupItem.setName(name);
                        groupItem.setInfo(info.toString());
                        groupItem.setDescription(description);
                        groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                        groupItemList.add(new AbstractMap.SimpleEntry<>(String.valueOf(id), groupItem));
                    }
                } catch (Exception e) {
                    Log.e(RequestViewModel.class.getSimpleName(), e.getMessage());
                }
            }
            initFirebaseData(groupItemList);
        }, error -> mState.postValue(new State(false, Collections.emptyList(), offset, false, false, error.getMessage()))) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "1");
                params.put("gubun", "select_share_total");
                params.put("start", String.valueOf(offset));
                params.put("display", String.valueOf(LIMIT));
                params.put("encoding", "utf-8");
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        params.forEach((k, v) -> {
                            try {
                                encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                encodedParams.append('=');
                                encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                encodedParams.append('&');
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        });
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                throw new RuntimeException();
            }
        });
    }

    public void fetchNextPage() {
        if (mState.getValue() != null && !mStopRequestMore) {
            mState.postValue(new State(false, mState.getValue().groupItemList, mState.getValue().offset, true, false, null));
        }
    }

    public void refresh() {
        mMinId = 0;

        Executors.newSingleThreadExecutor().execute(() -> mState.postValue(new State(false, Collections.emptyList(), 1, true, false, null)));
    }

    private void initFirebaseData(List<Map.Entry<String, GroupItem>> groupItemList) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(AppController.getInstance().getPreferenceManager().getUser().getUid()).orderByValue().equalTo(false), false, groupItemList);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion, List<Map.Entry<String, GroupItem>> groupItemList) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);

                        if (value != null) {
                            int index = groupItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(value.getId());

                            if (index > -1) {
                                GroupItem groupItem = groupItemList.get(index).getValue();

                                groupItemList.set(index, new AbstractMap.SimpleEntry<>(key, groupItem));
                            }
                        }
                    } catch (Exception e) {
                        mState.postValue(new State(false, Collections.emptyList(), 1, false, false, e.getMessage()));
                    } finally {
                        if (mState.getValue() != null && mState.getValue().groupItemList.size() != groupItemList.size()) {
                            mState.postValue(new State(false, mergedList(mState.getValue().groupItemList, groupItemList), mState.getValue().offset + LIMIT, false, groupItemList.isEmpty(), null));
                        }
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
                        if (mState.getValue() != null) {
                            mState.postValue(new State(false, mergedList(mState.getValue().groupItemList, groupItemList), 1, false, groupItemList.isEmpty(), null));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mState.postValue(new State(false, Collections.emptyList(), 0, false, false, databaseError.getMessage()));
            }
        });
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("'")[1].trim());
    }

    private List<Map.Entry<String, GroupItem>> mergedList(List<Map.Entry<String, GroupItem>> existingList, List<Map.Entry<String, GroupItem>> newList) {
        List<Map.Entry<String, GroupItem>> result = new ArrayList<>();

        result.addAll(existingList);
        result.addAll(newList);
        return result;
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, GroupItem>> groupItemList;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, GroupItem>> groupItemList, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }
    }
}

/*package com.hhp227.yu_minigroup.viewmodel;

        import android.webkit.CookieManager;

        import androidx.lifecycle.LiveData;
        import androidx.lifecycle.MutableLiveData;
        import androidx.lifecycle.ViewModel;

        import com.hhp227.yu_minigroup.app.AppController;
        import com.hhp227.yu_minigroup.app.EndPoint;
        import com.hhp227.yu_minigroup.data.GroupRepository;
        import com.hhp227.yu_minigroup.dto.GroupItem;
        import com.hhp227.yu_minigroup.helper.Callback;
        import com.hhp227.yu_minigroup.helper.PreferenceManager;

        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;
        import java.util.Map;
        import java.util.Objects;
        import java.util.concurrent.Executors;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, Collections.emptyList(), 1, false, false, null));

    private static final int LIMIT = 100;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    public RequestViewModel() {
        fetchNextPage();
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void fetchGroupList(int offset) {
        mGroupRepository.getJoinRequestGroupList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mPreferenceManager.getUser(), offset, LIMIT, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, GroupItem>> groupItemList = (List<Map.Entry<String, GroupItem>>) data;

                if (mState.getValue() != null) {
                    if (mState.getValue().groupItemList.size() != groupItemList.size()) {
                        mState.postValue(new State(false, mergedList(mState.getValue().groupItemList, groupItemList), mState.getValue().offset + LIMIT, false, groupItemList.isEmpty(), null));
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                mState.postValue(new State(false, Collections.emptyList(), 0, false, false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mState.postValue(new State(true, Objects.requireNonNull(mState.getValue()).groupItemList, offset, offset > 1, false, null));
            }
        });
    }

    public void fetchNextPage() {
        if (mState.getValue() != null && !mGroupRepository.isStopRequestMore()) {
            mState.postValue(new State(false, mState.getValue().groupItemList, mState.getValue().offset, true, false, null));
        }
    }

    public void refresh() {
        mGroupRepository.setMinId(0);
        Executors.newSingleThreadExecutor().execute(() -> mState.postValue(new State(false, Collections.emptyList(), 1, true, false, null)));
    }

    private List<Map.Entry<String, GroupItem>> mergedList(List<Map.Entry<String, GroupItem>> existingList, List<Map.Entry<String, GroupItem>> newList) {
        List<Map.Entry<String, GroupItem>> result = new ArrayList<>();

        result.addAll(existingList);
        result.addAll(newList);
        return result;
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, GroupItem>> groupItemList;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, GroupItem>> groupItemList, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }
    }
}
*/