package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ArticleRepository;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.*;

public class CreateArticleViewModel extends ViewModel {
    private final MutableLiveData<List<Object>> mContentList = new MutableLiveData<>();

    private static final String PROGRESS = "progress", ARTICLE_ID = "articleId", MESSAGE = "message";

    private List<String> mImageList;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private StringBuilder mMakeHtmlContents;

    private final String mArtlNum, mArtlKey;

    private final SavedStateHandle mSavedStateHandle;

    private final ArticleRepository articleRepository;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mArtlNum = savedStateHandle.get("artl_num");
        mArtlKey = savedStateHandle.get("artl_key");
        mImageList = savedStateHandle.get("img");
        articleRepository = new ArticleRepository(savedStateHandle.get("grp_id"), savedStateHandle.get("grp_key"));

        setContentList(new ArrayList<Object>() {
            {
                YouTubeItem youTubeItem = mSavedStateHandle.get("vid");

                add(new HashMap<String, MutableLiveData<String>>() {
                    {
                        String title = mSavedStateHandle.get("sbjt");
                        String content = mSavedStateHandle.get("txt");

                        put("title", new MutableLiveData<>(title != null ? title : ""));
                        put("content", new MutableLiveData<>(content != null ? content : ""));
                    }
                });
                if (mImageList != null && !mImageList.isEmpty()) {
                    addAll(mImageList);
                }
                if (youTubeItem != null && youTubeItem.position > -1) {
                    add(youTubeItem.position + 1, youTubeItem);
                }
            }
        });
    }

    public void setContentList(List<Object> contentList) {
        mContentList.postValue(contentList);
    }

    public LiveData<List<Object>> getContentList() {
        return mContentList;
    }

    public void setProgress(int progress) {
        mSavedStateHandle.set(PROGRESS, progress);
    }

    public LiveData<Integer> getProgress() {
        return mSavedStateHandle.getLiveData(PROGRESS);
    }

    public void setArticleId(String id) {
        mSavedStateHandle.set(ARTICLE_ID, id);
    }

    public LiveData<String> getArticleId() {
        return mSavedStateHandle.getLiveData(ARTICLE_ID);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public YouTubeItem getYouTubeItem() {
        List<Object> list = mContentList.getValue();

        if (list != null) {
            for (Object item : list) {
                if (item instanceof YouTubeItem) {
                    return (YouTubeItem) item;
                }
            }
        }
        return null;
    }

    public boolean hasYoutubeItem() {
        return getYouTubeItem() != null;
    }

    public <T> void addItem(T content) {
        setContentList(
                new ArrayList<Object>() {
                    {
                        addAll(Objects.requireNonNull(mContentList.getValue()));
                        add(content);
                    }
                }
        );
    }

    public <T> void addItem(int position, T content) {
        setContentList(
                new ArrayList<Object>() {
                    {
                        addAll(Objects.requireNonNull(mContentList.getValue()));
                        add(position, content);
                    }
                }
        );
    }

    public void removeItem(int position) {
        setContentList(
                new ArrayList<Object>() {
                    {
                        addAll(Objects.requireNonNull(mContentList.getValue()));
                        remove(position);
                    }
                }
        );
    }

    public void actionSend(Spannable spannableTitle, Spannable spannableContent, List<Object> contentList) {
        String title = spannableTitle.toString();
        String content = Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);

        if (!title.isEmpty() && !(TextUtils.isEmpty(content) && contentList.size() < 2)) {
            mMakeHtmlContents = new StringBuilder();
            mImageList = new ArrayList<>();

            setProgress(0);
            if (contentList.size() > 1) {
                int position = 1;

                itemTypeCheck(position, spannableTitle, spannableContent, contentList);
            } else {
                typeCheck(title, content);
            }
        } else {
            setMessage((title.isEmpty() ? "제목" : "내용") + "을 입력하세요.");
        }
    }

    private void typeCheck(String title, String content) {
        if (((int) mSavedStateHandle.get("type")) == 0) {
            actionCreate(title, content);
        } else {
            actionUpdate(title, content);
        }
    }

    private void actionCreate(final String title, final String content) {
        articleRepository.addArticle(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mPreferenceManager.getUser(), title, content, mImageList, getYouTubeItem(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setProgress(-1);
                setArticleId((String) data);
                setMessage("전송완료");
            }

            @Override
            public void onFailure(Throwable throwable) {
                setProgress(-1);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setProgress(0);
            }
        });
    }

    private void actionUpdate(final String title, final String content) {
        articleRepository.setArticle(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mArtlNum, mArtlKey, title, content, mImageList, getYouTubeItem(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                if (data != null) {
                    ArticleItem articleItem = (ArticleItem) data;

                    setProgress(-1);
                    setArticleId(articleItem.getId());
                    setMessage("수정완료");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                setProgress(-1);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setProgress(0);
            }
        });
    }

    private void uploadImage(final Spannable title, final Spannable content, final List<Object> contentList, final int position, final Bitmap bitmap) {
        articleRepository.addArticleImage(mCookieManager.getCookie(EndPoint.LOGIN_LMS), bitmap, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                uploadProcess(title, content, contentList, position, (String) data, false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                setProgress(-1);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setProgress(0);
            }
        });
    }

    private void uploadProcess(Spannable spannableTitle, Spannable spannableContent, List<Object> contentList, int position, String imageUrl, boolean isYoutube) {
        if (!isYoutube)
            mImageList.add(imageUrl);
        setProgress((int) ((double) (position) / (contentList.size() - 1) * 100));
        try {
            String test = (isYoutube ? "<p><embed title=\"YouTube video player\" class=\"youtube-player\" autostart=\"true\" src=\"//www.youtube.com/embed/" + imageUrl + "?autoplay=1\"  width=\"488\" height=\"274\"></embed><p>" // 유튜브 태그
                    : ("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>")) + (position < contentList.size() - 1 ? "<br>": "");

            mMakeHtmlContents.append(test);
            if (position < contentList.size() - 1) {
                position++;
                Thread.sleep(isYoutube ? 0 : 700);

                // 분기
                itemTypeCheck(position, spannableTitle, spannableContent, contentList);
            } else {
                String title = spannableTitle.toString();
                String content = (!TextUtils.isEmpty(spannableContent) ? Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlContents.toString();

                typeCheck(title, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setProgress(-1);
            setMessage("이미지 업로드 실패: " + e.getMessage());
        }
    }

    private void itemTypeCheck(int position, Spannable spannableTitle, Spannable spannableContent, List<Object> contentList) {
        if (contentList.get(position) instanceof String) {
            String image = (String) contentList.get(position);

            uploadProcess(spannableTitle, spannableContent, contentList, position, image, false);
        } else if (contentList.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
            Bitmap bitmap = (Bitmap) contentList.get(position);// 수정

            uploadImage(spannableTitle, spannableContent, contentList, position, bitmap); // 수정
        } else if (contentList.get(position) instanceof YouTubeItem) {
            YouTubeItem youTubeItem = (YouTubeItem) contentList.get(position);

            uploadProcess(spannableTitle, spannableContent, contentList, position, youTubeItem.videoId, true);
        }
    }
}
