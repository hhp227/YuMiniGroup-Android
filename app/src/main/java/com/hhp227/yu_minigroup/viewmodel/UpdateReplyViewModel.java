package com.hhp227.yu_minigroup.viewmodel;

import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ReplyRepository;
import com.hhp227.yu_minigroup.helper.Callback;

public class UpdateReplyViewModel extends ViewModel {
    public final MutableLiveData<String> text = new MutableLiveData<>("");

    private static final String LOADING = "loading", REPLY = "reply", MESSAGE = "message", REPLY_ERROR = "replyError";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final String mGroupId, mArticleId, mReplyId, mArticleKey, mReplyKey;

    private final SavedStateHandle mSavedStateHandle;

    private final ReplyRepository mReplyRepository;

    public UpdateReplyViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mArticleId = savedStateHandle.get("artl_num");
        mReplyId = savedStateHandle.get("cmmt_num");
        mArticleKey = savedStateHandle.get("artl_key");
        mReplyKey = savedStateHandle.get("cmmt_key");
        mReplyRepository = new ReplyRepository(mGroupId, mArticleId, mArticleKey);
        String reply = savedStateHandle.get("cmt");

        if (reply != null) {
            this.text.postValue(reply.contains("※") ? reply.substring(0, reply.lastIndexOf("※")).trim() : reply);
        }
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public void setReply(String reply) {
        mSavedStateHandle.set(REPLY, reply);
    }

    public LiveData<String> getReply() {
        return mSavedStateHandle.getLiveData(REPLY);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void setReplyError(String message) {
        mSavedStateHandle.set(REPLY_ERROR, message);
    }

    public LiveData<String> getReplyError() {
        return mSavedStateHandle.getLiveData(REPLY_ERROR);
    }

    public void actionSend(String text) {
        if (!TextUtils.isEmpty(text)) {
            mReplyRepository.setReply(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mReplyId, mReplyKey, text, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    setLoading(false);
                    setReply((String) data);
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
            setReplyError("내용을 입력하세요.");
        }
    }
}