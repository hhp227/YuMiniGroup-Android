package com.hhp227.yu_minigroup;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.yu_minigroup.adapter.ReplyListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.ReplyItem;
import com.hhp227.yu_minigroup.fragment.Tab1Fragment;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hhp227.yu_minigroup.fragment.Tab1Fragment.UPDATE_ARTICLE;

public class ArticleActivity extends AppCompatActivity {
    private static final int UPDATE_REPLY = 10;
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private boolean mIsBottom, mIsUpdate, mIsAuthorized;
    private int mPosition;
    private String mGroupId, mArticleId, mGroupName, mGroupKey, mArticleKey;
    private CardView mButtonSend;
    private EditText mInputReply;
    private ImageView mArticleProfile;
    private LinearLayout mArticleImages;
    private List<String> mImageList, mReplyItemKeys;
    private List<ReplyItem> mReplyItemValues;
    private ListView mListView;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private ReplyListAdapter mAdapter;
    private Source mSource;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mArticleTitle, mArticleTimeStamp, mArticleContent, mSendText;
    private View mArticleDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mArticleDetail = getLayoutInflater().inflate(R.layout.article_detail, null, false);
        mArticleProfile = mArticleDetail.findViewById(R.id.iv_profile_image);
        mArticleTitle = mArticleDetail.findViewById(R.id.tv_title);
        mArticleTimeStamp = mArticleDetail.findViewById(R.id.tv_timestamp);
        mArticleContent = mArticleDetail.findViewById(R.id.tv_content);
        mArticleImages = mArticleDetail.findViewById(R.id.ll_image);
        mButtonSend = findViewById(R.id.cv_btn_send);
        mInputReply = findViewById(R.id.et_reply);
        mListView = findViewById(R.id.lv_article);
        mSendText = findViewById(R.id.tv_btn_send);
        mSwipeRefreshLayout = findViewById(R.id.srl_article);
        mProgressBar = findViewById(R.id.pb_article);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        mArticleId = intent.getStringExtra("artl_num");
        mGroupKey = intent.getStringExtra("grp_key");
        mArticleKey = intent.getStringExtra("artl_key");
        mPosition = intent.getIntExtra("position", 0);
        mIsAuthorized = intent.getBooleanExtra("auth", false);
        mIsBottom = intent.getBooleanExtra("isbottom", false);
        mImageList = new ArrayList<>();
        mReplyItemKeys = new ArrayList<>();
        mReplyItemValues = new ArrayList<>();
        mAdapter = new ReplyListAdapter(this, mReplyItemKeys, mReplyItemValues);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mArticleDetail.setOnLongClickListener(v -> {
            v.showContextMenu();
            return true;
        });
        mButtonSend.setOnClickListener(v -> {
            if (mInputReply.getText().toString().trim().length() > 0) {
                actionSend(mInputReply.getText().toString());

                // 전송하면 텍스트 초기화
                mInputReply.setText("");
                if (v != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            } else
                Toast.makeText(getApplicationContext(), "댓글을 입력하세요.", Toast.LENGTH_LONG).show();
        });
        mInputReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mButtonSend.setCardBackgroundColor(getResources().getColor(s.length() > 0 ? R.color.colorAccent : androidx.cardview.R.color.cardview_light_background, null));
                mSendText.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray, null));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInputReply.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        });
        mListView.addHeaderView(mArticleDetail);
        mListView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(this::refresh, 1000));
        registerForContextMenu(mListView);
        showProgressBar();
        fetchArticleData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsAuthorized) {
            menu.add(Menu.NONE, 1, Menu.NONE, "수정하기");
            menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case 1:
                Intent modifyIntent = new Intent(this, ModifyActivity.class);
                modifyIntent.putExtra("grp_id", mGroupId);
                modifyIntent.putExtra("artl_num", mArticleId);
                modifyIntent.putExtra("sbjt", mArticleTitle.getText().toString().substring(0, mArticleTitle.getText().toString().lastIndexOf("-")).trim());
                modifyIntent.putExtra("txt", mArticleContent.getText().toString());
                modifyIntent.putStringArrayListExtra("img", (ArrayList<String>) mImageList);
                modifyIntent.putExtra("grp_key", mGroupKey);
                modifyIntent.putExtra("artl_key", mArticleKey);
                startActivityForResult(modifyIntent, UPDATE_ARTICLE);
                return true;
            case 2:
                String tag_string_req = "req_delete";

                showProgressBar();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_ARTICLE, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean("isError");
                        if (!error) {
                            Intent groupIntent = new Intent(ArticleActivity.this, GroupActivity.class);
                            groupIntent.putExtra("admin", getIntent().getBooleanExtra("admin", false));
                            groupIntent.putExtra("grp_id", mGroupId);
                            groupIntent.putExtra("grp_nm", mGroupName);
                            groupIntent.putExtra("key", mGroupKey);

                            // 모든 이전 activity 초기화
                            groupIntent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(groupIntent);
                            Toast.makeText(getApplicationContext(), "삭제완료", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "삭제할수 없습니다.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "json 파싱 에러 : " + e.getMessage());
                    } finally {
                        deleteArticleFromFirebase();
                    }
                    hideProgressBar();
                }, error -> {
                    Log.e(TAG, "전송 에러: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressBar();
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", mPreferenceManager.getCookie());
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("CLUB_GRP_ID", mGroupId);
                        params.put("ARTL_NUM", mArticleId);
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == RESULT_OK) {
            mIsUpdate = true;
            refresh();
        } else if (requestCode == UPDATE_REPLY && resultCode == RESULT_OK && data != null) {
            mSource = new Source(data.getStringExtra("update_reply"));
            mReplyItemKeys.clear();
            mReplyItemValues.clear();
            List<Element> commentList = mSource.getAllElementsByClass("comment-list");
            fetchReplyData(commentList);
        }
    }

    /**
     * 댓글을 길게 클릭하면 콘텍스트 메뉴가 뜸
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        menu.setHeaderTitle("작업선택");
        boolean auth = !mReplyItemValues.isEmpty() && position != 0 && mReplyItemValues.get((position - 1)).isAuth();
        menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
        if (position != 0 && auth) {
            menu.add(Menu.NONE, 2, Menu.NONE, "댓글 수정");
            menu.add(Menu.NONE, 3, Menu.NONE, "댓글 삭제");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String replyKey = mReplyItemKeys.isEmpty() || info.position == 0 ? null : mReplyItemKeys.get(info.position - 1);
        ReplyItem replyItem = mReplyItemValues.isEmpty() || info.position == 0 ? null : mReplyItemValues.get(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다.
        String replyId = replyItem == null ? "0" : replyItem.getId();
        switch (item.getItemId()) {
            case 1:
                android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(info.position == 0 ? mArticleContent.getText().toString() : replyItem.getReply());
                Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
                return true;
            case 2:
                Intent intent = new Intent(getBaseContext(), ReplyModifyActivity.class);
                String reply = replyItem.getReply();
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("artl_num", mArticleId);
                intent.putExtra("cmt", reply);
                intent.putExtra("cmmt_num", replyId);
                intent.putExtra("artl_key", mArticleKey);
                intent.putExtra("cmmt_key", replyKey);
                startActivityForResult(intent, UPDATE_REPLY);
                return true;
            case 3:
                String tag_string_req = "req_delete";

                showProgressBar();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_REPLY, response -> {
                    mSource = new Source(response);
                    hideProgressBar();
                    try {
                        if (!response.contains("처리를 실패했습니다")) {
                            mReplyItemKeys.clear();
                            mReplyItemValues.clear();
                            List<Element> commentList = mSource.getAllElementsByClass("comment-list");
                            fetchReplyData(commentList);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        deleteReplyFromFirebase(replyKey);
                    }
                }, error -> {
                    VolleyLog.e(TAG, error.getMessage());
                    hideProgressBar();
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", mPreferenceManager.getCookie());
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("CLUB_GRP_ID", mGroupId);
                        params.put("CMMT_NUM", replyId);
                        params.put("ARTL_NUM", mArticleId);
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                return true;
        }
        return false;
    }

    private void fetchArticleData() {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            mSource = new Source(response.trim());
            hideProgressBar();
            try {
                Element element = mSource.getFirstElementByClass("listbox2");
                Element viewArt = element.getFirstElementByClass("view_art");
                Element commentWrap = element.getFirstElementByClass("comment_wrap");
                List<Element> commentList = element.getAllElementsByClass("comment-list");

                String profileImg = null;
                String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                String title = listTitle.substring(0, listTitle.lastIndexOf("-")).trim();
                String name = listTitle.substring(listTitle.lastIndexOf("-") + 1).trim();
                String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                String content = contentExtractor(viewArt.getFirstElementByClass("list_cont"), true);
                List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();

                Glide.with(getApplicationContext())
                        .load(profileImg)
                        .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                        .into(mArticleProfile);
                mArticleTitle.setText(title + " - " + name);
                mArticleTimeStamp.setText(timeStamp);
                if (!TextUtils.isEmpty(content)) {
                    mArticleContent.setText(content);
                    mArticleContent.setVisibility(View.VISIBLE);
                } else
                    mArticleContent.setVisibility(View.GONE);

                if (images.size() > 0) {
                    mArticleImages.removeAllViews(); // 임시방편
                    for (Element image : images) {
                        if (mArticleImages.getChildCount() > images.size() - 1)
                            break;
                        int position = mImageList.size();
                        String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");
                        ImageView articleImage = new ImageView(getApplicationContext());
                        articleImage.setAdjustViewBounds(true);
                        articleImage.setPadding(0, 0, 0, 30);
                        articleImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        articleImage.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), PictureActivity.class);
                            intent.putStringArrayListExtra("images", (ArrayList<String>) mImageList);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        });
                        Glide.with(getApplicationContext()).load(imageUrl).apply(RequestOptions.errorOf(R.drawable.ic_launcher_background)).into(articleImage);
                        mArticleImages.addView(articleImage);
                        mImageList.add(imageUrl);
                    }
                    mArticleImages.setVisibility(View.VISIBLE);
                } else
                    mArticleImages.setVisibility(View.GONE);
                fetchReplyData(commentList);
                if (mIsUpdate)
                    deliveryUpdate(title, contentExtractor(viewArt.getFirstElementByClass("list_cont"), true), mImageList, replyCnt);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "값이 없습니다.", Toast.LENGTH_LONG).show();
            } finally {
                fetchArticleDataFromFirebase();
            }
        }, error -> {
            VolleyLog.d(TAG, "에러 : " + error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void fetchReplyData(List<Element> commentList) {
        try {
            for (Element comment : commentList) {
                Element commentName = comment.getFirstElementByClass("comment-name");
                Element commentAddr = comment.getFirstElementByClass("comment-addr");
                String replyId = commentAddr.getAttributeValue("id").replace("cmt_txt_", "");
                String name = commentName.getTextExtractor().toString().trim();
                String timeStamp = commentName.getFirstElement(HTMLElementName.SPAN).getContent().toString().trim();
                String replyContent = commentAddr.getContent().toString().trim();
                boolean authorization = commentName.getAllElements(HTMLElementName.INPUT).size() > 0;

                ReplyItem replyItem = new ReplyItem();
                replyItem.setId(replyId);
                replyItem.setName(name.substring(0, name.lastIndexOf("(")));
                replyItem.setReply(Html.fromHtml(replyContent).toString());
                replyItem.setDate(timeStamp.replaceAll("[(]|[)]", ""));
                replyItem.setAuth(authorization);
                mReplyItemKeys.add(replyId);
                mReplyItemValues.add(replyItem);
            }
            mAdapter.notifyDataSetChanged();
            if (mIsBottom)
                setListViewBottom();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            fetchReplyListFromFirebase();
        }
    }

    private void actionSend(final String text) {
        String tag_string_req = "req_send";

        showProgressBar();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.INSERT_REPLY, response -> {
            mSource = new Source(response);
            mReplyItemKeys.clear();
            mReplyItemValues.clear();
            List<Element> commentList = mSource.getAllElementsByClass("comment-list");
            try {
                fetchReplyData(commentList);
                hideProgressBar();

                // 전송할때마다 리스트뷰 아래로
                setListViewBottom();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                insertReplyToFirebase(commentList.get(commentList.size() - 1).getFirstElementByClass("comment-addr").getAttributeValue("id").replace("cmt_txt_", ""), text);
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", mArticleId);
                params.put("CMT", text);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    /**
     * 리스트뷰 하단으로 간다.
     */
    private void setListViewBottom() {
        new Handler().postDelayed(() -> {
            int articleHeight = mArticleDetail.getMeasuredHeight();
            mIsBottom = false;
            mListView.setSelection(articleHeight);
        }, 300);
    }

    private void deliveryUpdate(String title, String content, List<String> imageList, String replyCnt) {
        Intent intent = new Intent(getApplicationContext(), Tab1Fragment.class);
        intent.putExtra("position", mPosition);
        intent.putExtra("sbjt", title);
        intent.putExtra("txt", content);
        intent.putStringArrayListExtra("img", (ArrayList<String>) imageList);
        intent.putExtra("cmmt_cnt", replyCnt);

        setResult(RESULT_OK, intent);
    }

    private void refresh() {
        mIsUpdate = true;
        mImageList.clear();
        mReplyItemKeys.clear();
        mReplyItemValues.clear();
        mSwipeRefreshLayout.setRefreshing(false);
        fetchArticleData();
    }

    private String contentExtractor(Element listCont, boolean isFlag) {
        StringBuilder sb = new StringBuilder();
        for (Element childElement : isFlag ? listCont.getChildElements() : listCont.getAllElements(HTMLElementName.P))
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        return sb.toString().trim();
    }

    private void fetchArticleDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        databaseReference.child(mGroupKey).child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);
                    Glide.with(getApplicationContext())
                            .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                                    .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                                    .build()) : null)
                            .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                            .into(mArticleProfile);
                    mArticleTimeStamp.setText(new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private void deleteArticleFromFirebase() {
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");
        articlesReference.child(mGroupKey).child(mArticleKey).removeValue();
        replysReference.child(mArticleKey).removeValue();
    }

    private void fetchReplyListFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        databaseReference.child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        ReplyItem value = snapshot.getValue(ReplyItem.class);
                        int index = mReplyItemKeys.indexOf(value.getId());
                        if (index > -1) {
                            ReplyItem replyItem = mReplyItemValues.get(index);
                            replyItem.setUid(value.getUid());
                            mReplyItemValues.set(index, replyItem);
                            mReplyItemKeys.set(index, key);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private void insertReplyToFirebase(String replyId, String text) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();
        replyItem.setId(replyId);
        replyItem.setUid(mPreferenceManager.getUser().getUid());
        replyItem.setName(mPreferenceManager.getUser().getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);

        databaseReference.child(mArticleKey).push().setValue(replyItem);
    }

    private void deleteReplyFromFirebase(String replyKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        databaseReference.child(mArticleKey).child(replyKey).removeValue();
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

// 리사이클러뷰
/*public class ArticleActivity extends AppCompatActivity {
    private static final int UPDATE_REPLY = 10;
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private boolean mIsBottom, mIsUpdate, mIsAuthorized;
    private int mPosition;
    private String mGroupId, mArticleId, mGroupName, mTitle, mContent, mGroupKey, mArticleKey;
    private CardView mButtonSend;
    private EditText mInputReply;
    private List<String> mImageList, mReplyItemKeys;
    private List<Object> mReplyItemValues;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ReplyListAdapter mAdapter;
    private Source mSource;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mSendText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mButtonSend = findViewById(R.id.cv_btn_send);
        mInputReply = findViewById(R.id.et_reply);
        mSendText = findViewById(R.id.tv_btn_send);
        mSwipeRefreshLayout = findViewById(R.id.srl_article);
        mProgressBar = findViewById(R.id.pb_article);
        mRecyclerView = findViewById(R.id.rv_article);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        mArticleId = intent.getStringExtra("artl_num");
        mGroupKey = intent.getStringExtra("grp_key");
        mArticleKey = intent.getStringExtra("artl_key");
        mPosition = intent.getIntExtra("position", 0);
        mIsAuthorized = intent.getBooleanExtra("auth", false);
        mIsBottom = intent.getBooleanExtra("isbottom", false);
        mImageList = new ArrayList<>();
        mReplyItemKeys = new ArrayList<>();
        mReplyItemValues = new ArrayList<>();
        mAdapter = new ReplyListAdapter(this, mReplyItemKeys, mReplyItemValues);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mButtonSend.setOnClickListener(v -> {
            if (mInputReply.getText().toString().trim().length() > 0) {
                actionSend(mInputReply.getText().toString());

                // 전송하면 텍스트 초기화
                mInputReply.setText("");
                if (v != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            } else
                Toast.makeText(getApplicationContext(), "댓글을 입력하세요.", Toast.LENGTH_LONG).show();
        });
        mInputReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mButtonSend.setCardBackgroundColor(getResources().getColor(s.length() > 0 ? R.color.colorAccent : androidx.cardview.R.color.cardview_light_background, null));
                mSendText.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray, null));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInputReply.setOnFocusChangeListener((v, hasFocus) -> {

        });
        mAdapter.setHasStableIds(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(this::refresh, 1000));
        showProgressBar();
        fetchArticleData();

        if (mIsBottom)
            setListViewBottom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsAuthorized) {
            menu.add(Menu.NONE, 1, Menu.NONE, "수정하기");
            menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 1:
                Intent intent = new Intent(this, ModifyActivity.class);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("artl_num", mArticleId);
                intent.putExtra("sbjt", mTitle);
                intent.putExtra("txt", mContent);
                intent.putStringArrayListExtra("img", (ArrayList<String>) mImageList);
                intent.putExtra("grp_key", mGroupKey);
                intent.putExtra("artl_key", mArticleKey);
                startActivityForResult(intent, UPDATE_ARTICLE);
                return true;
            case 2:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == RESULT_OK) {
            refresh();
        } else if (requestCode == UPDATE_REPLY && resultCode == RESULT_OK && data != null) {

        }
    }

    private void fetchArticleData() {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            mSource = new Source(response.trim());
            try {
                Element element = mSource.getFirstElementByClass("listbox2");
                Element viewArt = element.getFirstElementByClass("view_art");
                Element commentWrap = element.getFirstElementByClass("comment_wrap");
                List<Element> commentList = element.getAllElementsByClass("comment-list");

                String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                String name = listTitle.substring(listTitle.lastIndexOf("-") + 1).trim();
                String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                mTitle = listTitle.substring(0, listTitle.lastIndexOf("-")).trim();
                mContent = contentExtractor(viewArt.getFirstElementByClass("list_cont"), true);

                List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();

                if (images.size() > 0)
                    images.forEach(image -> mImageList.add(!image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src")));

                ArticleItem articleItem = new ArticleItem();
                articleItem.setId(mArticleId);
                articleItem.setTitle(mTitle);
                articleItem.setName(name);
                articleItem.setDate(timeStamp);
                articleItem.setContent(mContent);
                articleItem.setImages(mImageList);
                articleItem.setReplyCount(replyCnt);
                articleItem.setAuth(mIsAuthorized);

                mReplyItemKeys.add(mArticleKey);
                mReplyItemValues.add(articleItem);
                mAdapter.notifyItemInserted(mReplyItemValues.size() - 1);
                
                fetchReplyData(commentList);
                if (mIsUpdate)
                    deliveryUpdate(mTitle, contentExtractor(viewArt.getFirstElementByClass("list_cont"), true), mImageList, replyCnt);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                fetchArticleDataFromFirebase();
            }
            hideProgressBar();
        }, error -> {
            VolleyLog.e(TAG, "에러 : " + error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void fetchReplyData(List<Element> commentList) {
        try {
            commentList.forEach(comment -> {
                Element commentName = comment.getFirstElementByClass("comment-name");
                Element commentAddr = comment.getFirstElementByClass("comment-addr");
                String replyId = commentAddr.getAttributeValue("id").replace("cmt_txt_", "");
                String name = commentName.getTextExtractor().toString().trim();
                String timeStamp = commentName.getFirstElement(HTMLElementName.SPAN).getContent().toString().trim();
                String replyContent = commentAddr.getContent().toString().trim();
                boolean authorization = commentName.getAllElements(HTMLElementName.INPUT).size() > 0;

                ReplyItem replyItem = new ReplyItem();
                replyItem.setId(replyId);
                replyItem.setName(name.substring(0, name.lastIndexOf("(")));
                replyItem.setReply(Html.fromHtml(replyContent).toString());
                replyItem.setDate(timeStamp.replaceAll("[(]|[)]", ""));
                replyItem.setAuth(authorization);
                mReplyItemKeys.add(replyId);
                mReplyItemValues.add(replyItem);
                mAdapter.notifyItemInserted(mReplyItemValues.size() - 1);
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            fetchReplyListFromFirebase();
        }
    }

    private void actionSend(String text) {
    }

    private void setListViewBottom() {
        new Handler().postDelayed(() -> mRecyclerView.scrollToPosition(mReplyItemValues.size() - 1), 300);
    }

    private void deliveryUpdate(String title, String content, List<String> imageList, String replyCnt) {
        Intent intent = new Intent(getApplicationContext(), Tab1Fragment.class);
        intent.putExtra("position", mPosition);
        intent.putExtra("sbjt", title);
        intent.putExtra("txt", content);
        intent.putStringArrayListExtra("img", (ArrayList<String>) imageList);
        intent.putExtra("cmmt_cnt", replyCnt);

        setResult(RESULT_OK, intent);
    }

    private void refresh() {
        mIsUpdate = true;
        mImageList.clear();
        mReplyItemKeys.clear();
        mReplyItemValues.clear();
        mSwipeRefreshLayout.setRefreshing(false);
        fetchArticleData();
    }

    private String contentExtractor(Element listCont, boolean isFlag) {
        StringBuilder sb = new StringBuilder();
        for (Element childElement : isFlag ? listCont.getChildElements() : listCont.getAllElements(HTMLElementName.P))
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        return sb.toString().trim();
    }

    private void fetchArticleDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        databaseReference.child(mGroupKey).child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int headerPosition = 0;
                    ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);
                    ArticleItem headerItem = (ArticleItem) mReplyItemValues.get(headerPosition);
                    headerItem.setUid(articleItem.getUid());
                    headerItem.setDate(new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
                    mReplyItemValues.set(headerPosition, headerItem);
                    //mAdapter.notifyItemChanged(headerPosition);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private void fetchReplyListFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        databaseReference.child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        ReplyItem value = snapshot.getValue(ReplyItem.class);
                        int index = mReplyItemKeys.indexOf(value.getId());
                        if (index > -1) {
                            ReplyItem replyItem = (ReplyItem) mReplyItemValues.get(index);
                            replyItem.setUid(value.getUid());
                            mReplyItemValues.set(index, replyItem);
                            mReplyItemKeys.set(index, key);
                        }
                        mAdapter.notifyItemChanged(index);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
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
*/