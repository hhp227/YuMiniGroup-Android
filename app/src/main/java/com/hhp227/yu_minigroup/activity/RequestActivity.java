package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.adapter.GroupListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityListBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.fragment.GroupInfoFragment;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {
    private static final int LIMIT = 100;

    private static final String TAG = RequestActivity.class.getSimpleName();

    private boolean mHasRequestedMore;

    private int mOffSet;

    private GroupListAdapter mAdapter;

    private List<String> mGroupItemKeys;

    private List<GroupItem> mGroupItemValues;

    private ActivityListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupListAdapter(this, mGroupItemKeys, mGroupItemValues);
        mOffSet = 1;

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.post(() -> {
            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.addFooterView();
            mAdapter.setButtonType(GroupInfoFragment.TYPE_CANCEL);
        });
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!mHasRequestedMore && dy > 0 && manager != null && manager.findLastCompletelyVisibleItemPosition() >= manager.getItemCount() - 1) {
                    mHasRequestedMore = true;
                    mOffSet += LIMIT;

                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    fetchGroupList();
                }
            }
        });
        mBinding.srlList.setOnRefreshListener(() -> new Handler(getMainLooper()).postDelayed(this::refresh, 1000));
        showProgressBar();
        new Handler(getMainLooper()).postDelayed(this::fetchGroupList, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.recyclerView.clearOnScrollListeners();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void fetchGroupList() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> list = source.getAllElements("id", "accordion", false);

            for (Element element : list) {
                try {
                    Element menuList = element.getFirstElementByClass("menu_list");

                    if (element.getAttributeValue("class").equals("accordion")) {
                        String id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                        String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        StringBuilder info = new StringBuilder();
                        String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                        String joinType = menuList.getAllElementsByClass("info").get(1).getContent().toString();

                        for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                            String extractedText = span.getTextExtractor().toString();

                            info.append(extractedText.contains("회원수") ?
                                    extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                    extractedText + "\n");
                        }
                        GroupItem groupItem = new GroupItem();

                        groupItem.setId(id);
                        groupItem.setImage(imageUrl);
                        groupItem.setName(name);
                        groupItem.setInfo(info.toString());
                        groupItem.setDescription(description);
                        groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                        mGroupItemKeys.add(mGroupItemKeys.size() - 1, id);
                        mGroupItemValues.add(mGroupItemValues.size() - 1, groupItem);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData();
                }
            }
            mHasRequestedMore = false;

            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
            hideProgressBar();
            mBinding.text.setText("가입신청중인 그룹이 없습니다.");
            mBinding.rlGroup.setVisibility(mGroupItemValues.size() > 1 ? View.GONE : View.VISIBLE);
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
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
                params.put("start", String.valueOf(mOffSet));
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

    public void refresh() {
        mOffSet = 1;

        mGroupItemKeys.clear();
        mGroupItemValues.clear();
        mAdapter.addFooterView();
        mBinding.srlList.setRefreshing(false);
        fetchGroupList();
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(AppController.getInstance().getPreferenceManager().getUser().getUid()).orderByValue().equalTo(false), false);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);
                        assert value != null;
                        int index = mGroupItemKeys.indexOf(value.getId());

                        if (index > -1) {
                            //mGroupItemValues.set(index, value); // isAdmin값때문에 주석처리
                            mGroupItemKeys.set(index, key);
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                        fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String onclick) {
        return onclick.split("'")[1].trim();
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
