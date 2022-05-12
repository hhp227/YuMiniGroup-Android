package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class RequestViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, Collections.emptyList(), Collections.emptyList(), 1, false, null));

    public List<String> mGroupItemKeys = new ArrayList<>(Arrays.asList(""));

    public List<GroupItem> mGroupItemValues = new ArrayList<>(Arrays.asList((GroupItem) null));

    private static final int LIMIT = 100;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private boolean mStopRequestMore = false;

    private int mMinId;

    public RequestViewModel() {
        fetchNextPage();
    }

    public void fetchGroupList(int offset) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> list = source.getAllElements("id", "accordion", false);
            List<String> groupItemKeys = new ArrayList<>();
            List<GroupItem> groupItemValues = new ArrayList<>();

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
                        groupItemKeys.add(String.valueOf(id));
                        groupItemValues.add(groupItem);
                    }
                } catch (Exception e) {
                    Log.e(RequestViewModel.class.getSimpleName(), e.getMessage());
                }
            }
            if (!groupItemKeys.isEmpty() && !groupItemValues.isEmpty()) {
                initFirebaseData(groupItemKeys, groupItemValues);
            } else {
                mState.postValue(new State(false, groupItemKeys, groupItemValues, offset, false, "데이터가 없습니다."));
            }
        }, error -> mState.postValue(new State(false, Collections.emptyList(), Collections.emptyList(), offset, false, error.getMessage()))) {
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
            mState.postValue(new State(false, Collections.emptyList(), Collections.emptyList(), mState.getValue().offset, true, null));
        }
    }

    public void refresh() {
        mMinId = 0;

        mGroupItemKeys.clear();
        mGroupItemValues.clear();
        mGroupItemKeys.add("");
        mGroupItemValues.add(null);
        Executors.newSingleThreadExecutor().execute(() -> mState.postValue(new State(false, Collections.emptyList(), Collections.emptyList(), 1, true, null)));
    }

    public void addAll(List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        if (groupItemKeys.size() == groupItemValues.size()) {
            mGroupItemKeys.addAll(mGroupItemKeys.size() - 1, groupItemKeys);
            mGroupItemValues.addAll(mGroupItemValues.size() - 1, groupItemValues);
        }
    }

    private void initFirebaseData(List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(AppController.getInstance().getPreferenceManager().getUser().getUid()).orderByValue().equalTo(false), false, groupItemKeys, groupItemValues);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion, List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);

                        if (value != null) {
                            int index = mGroupItemKeys.indexOf(value.getId());

                            if (index > -1) {
                                groupItemKeys.set(index, key);
                                //groupItemValues.set(index, value); //isAdmin값때문에 주석처리
                            }
                        }
                    } catch (Exception e) {
                        mState.postValue(new State(false, Collections.emptyList(), Collections.emptyList(), 0, false, e.getMessage()));
                    } finally {
                        if (mState.getValue() != null && mState.getValue().groupItemKeys.size() != groupItemKeys.size() && mState.getValue().groupItemValues.size() != groupItemValues.size()) {
                            mState.postValue(new State(false, groupItemKeys, groupItemValues, mState.getValue().offset + LIMIT, false, null));
                        }
                    }
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                        String key = snapshot.getKey();

                        if (key != null) {
                            fetchDataTaskFromFirebase(databaseReference.child(key), true, groupItemKeys, groupItemValues);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mState.postValue(new State(false, Collections.emptyList(), Collections.emptyList(), 0, false, databaseError.getMessage()));
            }
        });
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("'")[1].trim());
    }


    public static final class State {
        public boolean isLoading;

        public List<String> groupItemKeys;

        public List<GroupItem> groupItemValues;

        public int offset;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, List<String> groupItemKeys, List<GroupItem> groupItemValues, int offset, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.groupItemKeys = groupItemKeys;
            this.groupItemValues = groupItemValues;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }
}
