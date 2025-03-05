package com.hhp227.yu_minigroup.activity;

import static com.hhp227.yu_minigroup.viewmodel.YoutubeSearchViewModel.API_KEY;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.ReplyListAdapter;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityArticleBinding;
import com.hhp227.yu_minigroup.databinding.ArticleDetailBinding;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.ReplyItem;
import com.hhp227.yu_minigroup.fragment.Tab1Fragment;
import com.hhp227.yu_minigroup.helper.DateUtil;
import com.hhp227.yu_minigroup.helper.MyYouTubeBaseActivity;
import com.hhp227.yu_minigroup.viewmodel.ArticleViewModel;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

// TODO
public class ArticleActivity extends MyYouTubeBaseActivity {
    private static final int UPDATE_ARTICLE = 10;

    private static final int UPDATE_REPLY = 20;

    private ReplyListAdapter mAdapter;

    private TextWatcher mTextWatcher;

    private ActivityArticleBinding mActivityArticleBinding;

    private ArticleDetailBinding mArticleDetailBinding;

    private ArticleViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityArticleBinding = ActivityArticleBinding.inflate(getLayoutInflater());
        mArticleDetailBinding = ArticleDetailBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);
        mAdapter = new ReplyListAdapter();
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mActivityArticleBinding.cvBtnSend.setCardBackgroundColor(getResources().getColor(s.length() > 0 ? com.hhp227.yu_minigroup.R.color.colorAccent : androidx.cardview.R.color.cardview_light_background, null));
                mActivityArticleBinding.tvBtnSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray, null));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        setContentView(mActivityArticleBinding.getRoot());
        setSupportActionBar(mActivityArticleBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mArticleDetailBinding.getRoot().setOnLongClickListener(v -> {
            v.showContextMenu();
            return true;
        });
        mActivityArticleBinding.cvBtnSend.setOnClickListener(v -> {
            String text = mActivityArticleBinding.etReply.getText().toString().trim();

            mViewModel.actionSend(text);
            if (!TextUtils.isEmpty(text)) {

                // 전송하면 텍스트 초기화
                mActivityArticleBinding.etReply.setText("");
            }
        });
        mActivityArticleBinding.etReply.addTextChangedListener(mTextWatcher);
        mActivityArticleBinding.etReply.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                mActivityArticleBinding.lvArticle.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        });
        mActivityArticleBinding.lvArticle.addHeaderView(mArticleDetailBinding.getRoot());
        mActivityArticleBinding.lvArticle.setAdapter(mAdapter);
        mActivityArticleBinding.srlArticle.setOnRefreshListener(() -> new Handler(getMainLooper()).postDelayed(this::refresh, 1000));
        registerForContextMenu(mActivityArticleBinding.lvArticle);
        mViewModel.getState().observe(this, state -> {
            if (state.isLoading) {
                showProgressBar();
            } else if (state.isSetResultOK) {
                setResult(RESULT_OK);
                finish();
                Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
            } else if (state.articleItem != null || state.replyItemList != null) {
                mAdapter.submitList(state.replyItemList);
                hideProgressBar();
                if (state.articleItem != null) {
                    mViewModel.setArticleState(state.articleItem);
                    if (mViewModel.getUpdateArticleState().getValue() != null) {
                        deliveryUpdate(state.articleItem);
                    }
                }
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(mActivityArticleBinding.lvArticle, state.message, Snackbar.LENGTH_LONG).show();
            }
        });
        mViewModel.getReplyFormState().observe(this, replyFormState -> Toast.makeText(ArticleActivity.this, replyFormState.replyError, Toast.LENGTH_SHORT).show());
        mViewModel.getArticleState().observe(this, this::bindArticle);
        mViewModel.getUpdateArticleState().observe(this, isUpdateArticle -> mViewModel.refresh());
        mViewModel.getScrollToLastState().observe(this, isScrollToLast -> {
            if (isScrollToLast) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(mActivityArticleBinding.lvArticle.getWindowToken(), 0);
                setListViewBottom();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityArticleBinding.etReply.removeTextChangedListener(mTextWatcher);
        mTextWatcher = null;
        mActivityArticleBinding = null;
        mArticleDetailBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mViewModel.mIsAuthorized) {
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
                Intent modifyIntent = new Intent(this, CreateArticleActivity.class);
                ArticleItem articleItem = mViewModel.getArticleState().getValue();

                if (articleItem != null) {
                    modifyIntent.putExtra("grp_id", mViewModel.mGroupId);
                    modifyIntent.putExtra("artl_num", articleItem.getId());
                    modifyIntent.putExtra("sbjt", articleItem.getTitle());
                    modifyIntent.putExtra("txt", articleItem.getContent());
                    modifyIntent.putStringArrayListExtra("img", (ArrayList<String>) articleItem.getImages());
                    modifyIntent.putExtra("vid", articleItem.getYoutube());
                    modifyIntent.putExtra("grp_key", mViewModel.mGroupKey);
                    modifyIntent.putExtra("artl_key", mViewModel.mArticleKey);
                    modifyIntent.putExtra("type", 1);
                    startActivityForResult(modifyIntent, UPDATE_ARTICLE);
                }
                return true;
            case 2:
                mViewModel.deleteArticle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == RESULT_OK) {
            mViewModel.setUpdateArticleState(true);
        } else if (requestCode == UPDATE_REPLY && resultCode == RESULT_OK && data != null) {
            Source source = new Source(data.getStringExtra("update_reply"));
            List<Element> commentList = source.getAllElementsByClass("comment-list");

            mViewModel.refreshReply(commentList);
        }
    }

    /**
     * 댓글을 길게 클릭하면 콘텍스트 메뉴가 뜸
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        boolean auth = !mAdapter.getCurrentList().isEmpty() && position != 0 && mAdapter.getCurrentList().get((position - 1)).getValue().isAuth();

        menu.setHeaderTitle("작업선택");
        menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
        if (position != 0 && auth) {
            menu.add(Menu.NONE, 2, Menu.NONE, "댓글 수정");
            menu.add(Menu.NONE, 3, Menu.NONE, "댓글 삭제");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String replyKey = mAdapter.getCurrentList().isEmpty() || info.position == 0 ? null : mAdapter.getCurrentList().get(info.position - 1).getKey();
        ReplyItem replyItem = mAdapter.getCurrentList().isEmpty() || info.position == 0 ? null : mAdapter.getCurrentList().get(info.position - 1).getValue(); // 헤더가 있기때문에 포지션에서 -1을 해준다.

        if (replyItem != null) {
            String replyId = replyItem.getId();

            switch (item.getItemId()) {
                case 1:
                    android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                    clipboard.setText(info.position == 0 ? mArticleDetailBinding.tvContent.getText().toString() : replyItem.getReply());
                    Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
                    return true;
                case 2:
                    Intent intent = new Intent(getBaseContext(), UpdateReplyActivity.class);
                    String reply = replyItem.getReply();

                    intent.putExtra("grp_id", mViewModel.mGroupId);
                    intent.putExtra("artl_num", mViewModel.mArticleId);
                    intent.putExtra("cmt", reply);
                    intent.putExtra("cmmt_num", replyId);
                    intent.putExtra("artl_key", mViewModel.mArticleKey);
                    intent.putExtra("cmmt_key", replyKey);
                    startActivityForResult(intent, UPDATE_REPLY);
                    return true;
                case 3:
                    mViewModel.deleteReply(replyId, replyKey);
                    return true;
            }
        }
        return false;
    }

    private void bindArticle(final ArticleItem articleItem) {
        Glide.with(getApplicationContext())
                .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", mViewModel.getCookie())
                        .build()) : null)
                .apply(RequestOptions
                        .errorOf(R.drawable.user_image_view_circle)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mArticleDetailBinding.ivProfileImage);
        mArticleDetailBinding.tvTitle.setText(articleItem.getTitle() + " - " + articleItem.getName());
        mArticleDetailBinding.tvTimestamp.setText(DateUtil.getDateString(articleItem.getTimestamp()));
        mArticleDetailBinding.tvContent.setText(articleItem.getContent());
        mArticleDetailBinding.tvContent.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) ? View.VISIBLE : View.GONE);
        if ((articleItem.getImages() != null && !articleItem.getImages().isEmpty()) || articleItem.getYoutube() != null) {
            mArticleDetailBinding.llImage.removeAllViews();
            if (articleItem.getImages() != null) {
                for (int i = 0; i < articleItem.getImages().size(); i++) {
                    if (mArticleDetailBinding.llImage.getChildCount() > articleItem.getImages().size() - 1)
                        break;
                    int position = i;
                    ImageView articleImage = new ImageView(this);

                    articleImage.setAdjustViewBounds(true);
                    articleImage.setPadding(0, 0, 0, 30);
                    articleImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    articleImage.setOnClickListener(v -> {
                        Intent intent = new Intent(this, PictureActivity.class);

                        intent.putStringArrayListExtra("images", (ArrayList<String>) articleItem.getImages());
                        intent.putExtra("position", position);
                        startActivity(intent);
                    });
                    Glide.with(this)
                            .load(articleItem.getImages().get(i))
                            .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                            .into(articleImage);
                    mArticleDetailBinding.llImage.addView(articleImage);
                }
            }
            if (articleItem.getYoutube() != null) {
                LinearLayout youtubeContainer = new LinearLayout(this);
                YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this);

                youTubePlayerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        youTubePlayer.setShowFullscreenButton(true);
                        if (b) {
                            youTubePlayer.play();
                        } else {
                            try {
                                youTubePlayer.cueVideo(articleItem.getYoutube().videoId);
                            } catch (IllegalStateException e) {
                                youTubePlayerView.initialize(API_KEY, this);
                            }
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                        try {
                            if (youTubeInitializationResult.isUserRecoverableError())
                                youTubeInitializationResult.getErrorDialog(getParent(), 0).show();
                        } catch (Exception e) {
                            if (e.getMessage() != null) {
                                Log.e(ArticleActivity.class.getSimpleName(), e.getMessage());
                            }
                        }
                    }
                });
                youtubeContainer.addView(youTubePlayerView);
                youtubeContainer.setPadding(0, 0, 0, 30);
                mArticleDetailBinding.llImage.addView(youtubeContainer, articleItem.getYoutube().position);
            }
            mArticleDetailBinding.llImage.setVisibility(View.VISIBLE);
        } else {
            mArticleDetailBinding.llImage.setVisibility(View.GONE);
        }
    }

    /**
     * 리스트뷰 하단으로 간다.
     */
    private void setListViewBottom() {
        new Handler(getMainLooper()).postDelayed(() -> {
            int articleHeight = mArticleDetailBinding.getRoot().getMeasuredHeight();

            mActivityArticleBinding.lvArticle.setSelection(articleHeight);
        }, 500);
    }

    private void deliveryUpdate(ArticleItem articleItem) {
        Intent intent = new Intent(getApplicationContext(), Tab1Fragment.class);

        intent.putExtra("position", mViewModel.mPosition);
        intent.putExtra("sbjt", articleItem.getTitle());
        intent.putExtra("txt", articleItem.getContent());
        intent.putStringArrayListExtra("img", (ArrayList<String>) articleItem.getImages());
        intent.putExtra("cmmt_cnt", articleItem.getReplyCount());
        intent.putExtra("youtube", articleItem.getYoutube());
        setResult(RESULT_OK, intent);
    }

    private void refresh() {
        mViewModel.refresh();
        mActivityArticleBinding.srlArticle.setRefreshing(false);
    }

    private void showProgressBar() {
        if (mActivityArticleBinding.pbArticle.getVisibility() == View.GONE)
            mActivityArticleBinding.pbArticle.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mActivityArticleBinding.pbArticle.getVisibility() == View.VISIBLE)
            mActivityArticleBinding.pbArticle.setVisibility(View.GONE);
    }
}