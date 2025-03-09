package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ArticleRepository;
import com.hhp227.yu_minigroup.data.ReplyRepository;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.ReplyItem;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArticleViewModel extends ViewModel {
    public final Boolean mIsAuthorized;

    public final Integer mPosition;

    public final String mGroupId, mArticleId, mGroupName, mGroupImage, mGroupKey, mArticleKey;

    public final MutableLiveData<String> reply = new MutableLiveData<>("");

    private static final String UPDATE_ARTICLE_STATE = "updateArticleState", ARTICLE = "article", LOADING = "loading", SUCCESS = "success", MESSAGE = "message", REPLY_ERROR = "replyError";

    private final MutableLiveData<List<Map.Entry<String, ReplyItem>>> mReplyItemList = new MutableLiveData<>(Collections.emptyList());

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private final ArticleRepository mArticleRepository;

    private final ReplyRepository mReplyRepository;

    public ArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("grp_img");
        mArticleId = savedStateHandle.get("artl_num");
        mGroupKey = savedStateHandle.get("grp_key");
        mArticleKey = savedStateHandle.get("artl_key");
        mPosition = savedStateHandle.get("position");
        mIsAuthorized = savedStateHandle.get("auth");
        mArticleRepository = new ArticleRepository(mGroupId, mGroupKey);
        mReplyRepository = new ReplyRepository(mGroupId, mArticleId, mArticleKey);

        fetchArticleData(mArticleId, false);
        setArticle(new ArticleItem("", "", "", "", "", Collections.emptyList(), null, "", false, 0));
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public LiveData<List<Map.Entry<String, ReplyItem>>> getReplyItemList() {
        return mReplyItemList;
    }

    public void setSuccess(boolean bool) {
        mSavedStateHandle.set(SUCCESS, bool);
    }

    public LiveData<Boolean> isSuccess() {
        return mSavedStateHandle.getLiveData(SUCCESS);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void setReplyError(String text) {
        mSavedStateHandle.set(REPLY_ERROR, text);
    }

    public LiveData<String> getReplyError() {
        return mSavedStateHandle.getLiveData(REPLY_ERROR);
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public void setArticleUpdated(boolean bool) {
        mSavedStateHandle.set(UPDATE_ARTICLE_STATE, bool);
    }

    public LiveData<Boolean> isArticleUpdated() {
        return mSavedStateHandle.getLiveData(UPDATE_ARTICLE_STATE);
    }

    public void setArticle(ArticleItem articleItem) {
        mSavedStateHandle.set(ARTICLE, articleItem);
    }

    public LiveData<ArticleItem> getArticle() {
        return mSavedStateHandle.getLiveData(ARTICLE);
    }

    public void setScrollToLastState(boolean bool) {
        mSavedStateHandle.set("isbottom", bool);
    }

    public LiveData<Boolean> getScrollToLastState() {
        return mSavedStateHandle.getLiveData("isbottom");
    }

    public void actionSend(String text) {
        if (!text.isEmpty()) {
            setLoading(true);
            reply.postValue("");
            mReplyRepository.addReply(getCookie(), mPreferenceManager.getUser(), text, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    refreshReply((List<Element>) data);

                    // 전송할때마다 리스트뷰 아래로
                    setScrollToLastState(true);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    setLoading(false);
                    setMessage(throwable.getMessage());
                }

                @Override
                public void onLoading() {
                    setLoading(true);
                }
            });
        } else {
            setReplyError("댓글을 입력하세요.");
        }
    }

    public void deleteArticle() {
        mArticleRepository.removeArticle(getCookie(), mArticleId, mArticleKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                setSuccess(true);
                setMessage("삭제완료");
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }

    public void deleteReply(String replyId, String replyKey) {
        mReplyRepository.removeReply(getCookie(), replyId, replyKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                refreshReply((List<Element>) data);
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }

    public void refresh() {
        fetchArticleData(mArticleId, true);
    }

    public void refreshReply(List<Element> commentList) {
        fetchReplyData(commentList);
    }

    private void fetchArticleData(String articleId, boolean isUpdated) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";

        mArticleRepository.getArticleData(getCookie(), articleId, mArticleKey, params, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                if (data instanceof ArticleItem) {
                    ArticleItem articleItem = (ArticleItem) data;

                    setLoading(false);
                    setArticle(articleItem);
                    if (isUpdated) setArticleUpdated(true);
                } else {
                    refreshReply((List<Element>) data);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }

    private void fetchReplyData(List<Element> commentList) {
        mReplyRepository.getReplyList(commentList, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                mReplyItemList.postValue((List<Map.Entry<String, ReplyItem>>) data);
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {}
        });
    }
}
