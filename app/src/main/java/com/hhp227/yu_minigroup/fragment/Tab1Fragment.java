package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.ArticleActivity;
import com.hhp227.yu_minigroup.activity.CreateArticleActivity;
import com.hhp227.yu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentTab1Binding;
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

    public static boolean mIsAdmin;

    public static String mGroupId, mGroupName, mGroupImage, mKey;

    private static final String TAG = "소식";

    private boolean mHasRequestedMore;

    private int mOffSet;

    private long  mMinId, mLastClickTime;

    private ArticleListAdapter mAdapter;

    private List<String> mArticleItemKeys;

    private List<ArticleItem> mArticleItemValues;

    private FragmentTab1Binding mBinding;

    private ActivityResultLauncher<Intent> mArticleActivityResultLauncher;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab1Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mArticleItemKeys = new ArrayList<>();
        mArticleItemValues = new ArrayList<>();
        mAdapter = new ArticleListAdapter(mArticleItemKeys, mArticleItemValues, mKey);
        mOffSet = 1;
        mArticleActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    int position = result.getData().getIntExtra("position", 0) - 1;
                    ArticleItem articleItem = mArticleItemValues.get(position);

                    articleItem.setTitle(result.getData().getStringExtra("sbjt"));
                    articleItem.setContent(result.getData().getStringExtra("txt"));
                    articleItem.setImages(result.getData().getStringArrayListExtra("img")); // firebase data
                    articleItem.setReplyCount(result.getData().getStringExtra("cmmt_cnt"));
                    articleItem.setYoutube(result.getData().getParcelableExtra("youtube"));
                    mArticleItemValues.set(position, articleItem);
                    mAdapter.notifyItemChanged(position);
                } else {
                    refreshArticleList();
                    mBinding.rvArticle.scrollToPosition(0);
                    ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
                }
            }
        });

        mBinding.rvArticle.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvArticle.setAdapter(mAdapter);
        mBinding.rvArticle.post(() -> {
            mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
            mAdapter.addFooterView();
        });
        mBinding.rvArticle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
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
            mArticleActivityResultLauncher.launch(intent);
        });
        mBinding.rlWrite.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                return;
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(getActivity(), CreateArticleActivity.class);

            intent.putExtra("grp_id", mGroupId);
            intent.putExtra("grp_nm", mGroupName);
            intent.putExtra("grp_img", mGroupImage);
            intent.putExtra("grp_key", mKey);
            intent.putExtra("type", 0);
            ((TabHostLayoutFragment) requireParentFragment()).mCreateArticleResultLauncher.launch(intent);
        });
        mBinding.srlArticleList.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            refreshArticleList();
            mBinding.srlArticleList.setRefreshing(false);
        }, 2000));
        mBinding.srlArticleList.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);
        showProgressBar();
        fetchArticleList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mArticleActivityResultLauncher = null;
    }

    public void onCreateArticleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            refreshArticleList();
            mBinding.rvArticle.scrollToPosition(0);
            ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
        }
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
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
                    String replyCnt = commentWrap.getFirstElementByClass("commentBtn").getTextExtractor().toString(); // 댓글 + commentWrap.getFirstElementByClass("comment_cnt").getTextExtractor();

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
            mBinding.rlWrite.setVisibility(mArticleItemValues.size() > 1 ? View.GONE : View.VISIBLE);
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

    private void refreshArticleList() {
        mMinId = 0;
        mOffSet = 1;

        mArticleItemKeys.clear();
        mArticleItemValues.clear();
        mAdapter.addFooterView();
        fetchArticleList();
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mKey));
    }

    private void fetchArticleListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    private void showProgressBar() {
        if (mBinding.pbArticle.getVisibility() == View.GONE)
            mBinding.pbArticle.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbArticle.getVisibility() == View.VISIBLE)
            mBinding.pbArticle.setVisibility(View.GONE);
    }
}
