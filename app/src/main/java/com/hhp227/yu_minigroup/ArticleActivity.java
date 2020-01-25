package com.hhp227.yu_minigroup;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
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
    private String mGroupId, mArticleId, mGroupName, mTitle, mContent, mGroupKey, mArticleKey;
    private CardView mButtonSend;
    private EditText mInputReply;
    private List<String> mImageList, mReplyItemKeys;
    private List<Object> mReplyItemValues;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private ReplyListAdapter mAdapter;
    private Source mSource;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mSendText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerView = findViewById(R.id.rv_article);
        mButtonSend = findViewById(R.id.cv_btn_send);
        mInputReply = findViewById(R.id.et_reply);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
        }, 1000));
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
        }
        return super.onOptionsItemSelected(item);
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
                mAdapter.notifyItemInserted(0);
                
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
