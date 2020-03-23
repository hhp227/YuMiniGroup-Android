package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.ArticleActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.WriteActivity;
import com.hhp227.yu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends Fragment {
    public static final int LIMIT = 10;
    public static final int UPDATE_ARTICLE = 20;
    public static boolean mIsAdmin;
    public static String mGroupId, mGroupName, mGroupImage, mKey;

    private static final String TAG = "소식";
    private boolean mHasRequestedMore;
    private int mOffSet;
    private long  mMinId, mLastClickTime;
    private ArticleListAdapter mAdapter;
    private List<String> mArticleItemKeys;
    private List<ArticleItem> mArticleItemValues;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;

    public Tab1Fragment() {
    }

    public static Tab1Fragment newInstance(boolean isAdmin, String grpId, String grpNm, String grpImg, String key) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_nm", grpNm);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsAdmin = getArguments().getBoolean("admin");
            mGroupId = getArguments().getString("grp_id");
            mGroupName = getArguments().getString("grp_nm");
            mGroupImage = getArguments().getString("grp_img");
            mKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.srl_article_list);
        RecyclerView recyclerView = view.findViewById(R.id.rv_article);
        mProgressBar = view.findViewById(R.id.pb_article);
        mRelativeLayout = view.findViewById(R.id.rl_write);
        mArticleItemKeys = new ArrayList<>();
        mArticleItemValues = new ArrayList<>();
        mAdapter = new ArticleListAdapter(getActivity(), mArticleItemKeys, mArticleItemValues, mKey);
        mOffSet = 1;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.post(() -> {
            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.addFooterView();
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!mHasRequestedMore && dy > 0 && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
                    mHasRequestedMore = true;
                    mOffSet += LIMIT;

                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    fetchArticleList();
                }
            }
        });
        mAdapter.setOnItemClickListener((v, position) -> {
            ArticleItem articleItem = mArticleItemValues.get(position);
            Intent intent = new Intent(getContext(), ArticleActivity.class);

            intent.putExtra("admin", mIsAdmin);
            intent.putExtra("grp_id", mGroupId);
            intent.putExtra("grp_nm", mGroupName);
            intent.putExtra("grp_img", mGroupImage);
            intent.putExtra("artl_num", articleItem.getId());
            intent.putExtra("position", position + 1);
            intent.putExtra("auth", articleItem.isAuth() || AppController.getInstance().getPreferenceManager().getUser().getUid().equals(articleItem.getUid()));
            intent.putExtra("isbottom", v.getId() == R.id.ll_reply);
            intent.putExtra("grp_key", mKey);
            intent.putExtra("artl_key", mAdapter.getKey(position));
            startActivityForResult(intent, UPDATE_ARTICLE);
        });
        mRelativeLayout.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                return;
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(getActivity(), WriteActivity.class);

            intent.putExtra("admin", mIsAdmin);
            intent.putExtra("grp_id", mGroupId);
            intent.putExtra("grp_nm", mGroupName);
            intent.putExtra("grp_img", mGroupImage);
            intent.putExtra("key", mKey);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mMinId = 0;
            mOffSet = 1;

            mArticleItemKeys.clear();
            mArticleItemValues.clear();
            mAdapter.addFooterView();
            swipeRefreshLayout.setRefreshing(false);
            fetchArticleList();
        }, 2000));
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);
        showProgressBar();
        fetchArticleList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == Activity.RESULT_OK) {
            int position = data.getIntExtra("position", 0) - 1;
            ArticleItem articleItem = mArticleItemValues.get(position);

            articleItem.setTitle(data.getStringExtra("sbjt"));
            articleItem.setContent(data.getStringExtra("txt"));
            articleItem.setImages(data.getStringArrayListExtra("img")); // firebase data
            articleItem.setReplyCount(data.getStringExtra("cmmt_cnt"));
            articleItem.setYoutube(data.getParcelableExtra("youtube"));
            mArticleItemValues.set(position, articleItem);
            mAdapter.notifyItemChanged(position);
        } else if (resultCode == Activity.RESULT_OK)
            mAdapter.notifyDataSetChanged();
    }

    private void fetchArticleList() {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mOffSet + "&displayL=" + LIMIT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);

            hideProgressBar();
            try {
                List<Element> list = source.getAllElementsByClass("listbox2");
                for (Element element : list) {
                    Element viewArt = element.getFirstElementByClass("view_art");
                    Element commentWrap = element.getFirstElementByClass("comment_wrap");
                    boolean auth = viewArt.getAllElementsByClass("btn-small-gray").size() > 0;
                    String id = commentWrap.getAttributeValue("num");
                    String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                    String title = listTitle.substring(0, listTitle.lastIndexOf("-"));
                    String name = listTitle.substring(listTitle.lastIndexOf("-") + 1);
                    String date = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                    List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                    StringBuilder content = new StringBuilder();
                    List<String> imageList = new ArrayList<>();
                    String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();

                    if (images.size() > 0)
                        images.forEach(image -> imageList.add(!image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src")));
                    viewArt.getFirstElementByClass("list_cont").getChildElements().forEach(childElement -> content.append(childElement.getTextExtractor().toString().concat("\n")));

                    mMinId = mMinId == 0 ? Long.parseLong(id) : Math.min(mMinId, Long.parseLong(id));
                    if (Long.parseLong(id) > mMinId) {
                        mHasRequestedMore = true;
                        break;
                    } else
                        mHasRequestedMore = false;
                    ArticleItem articleItem = new ArticleItem();

                    articleItem.setId(id);
                    articleItem.setTitle(title.trim());
                    articleItem.setName(name.trim());
                    articleItem.setDate(date);
                    articleItem.setContent(content.toString().trim());
                    articleItem.setImages(imageList);
                    articleItem.setReplyCount(replyCnt);
                    articleItem.setAuth(auth);
                    if (viewArt.getFirstElementByClass("youtube-player") != null) {
                        String youtubeUrl = viewArt.getFirstElementByClass("youtube-player").getAttributeValue("src");
                        String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                        String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                        YouTubeItem youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);

                        articleItem.setYoutube(youTubeItem);
                    }

                    mArticleItemKeys.add(mArticleItemKeys.size() - 1, id);
                    mArticleItemValues.add(mArticleItemValues.size() - 1, articleItem);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                initFirebaseData();
            }
            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
            mRelativeLayout.setVisibility(mArticleItemValues.size() > 1 ? View.GONE : View.VISIBLE);
        }, error -> {
            VolleyLog.e(error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mKey));
    }

    private void fetchArticleListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);
                    int index = mArticleItemKeys.indexOf(value.getId());
                    if (index > -1) {
                        ArticleItem articleItem = mArticleItemValues.get(index);

                        articleItem.setUid(value.getUid());
                        mArticleItemValues.set(index, articleItem);
                        mArticleItemKeys.set(index, key);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("파이어베이스", databaseError.getMessage());
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
