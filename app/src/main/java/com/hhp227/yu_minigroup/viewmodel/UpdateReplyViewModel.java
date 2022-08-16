package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ReplyRepository;
import com.hhp227.yu_minigroup.helper.Callback;

public class UpdateReplyViewModel extends ViewModel {
    private static final String STATE = "state", REPLY_FORM_STATE = "replyFormState";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final String mGroupId, mArticleId, mReplyId, mArticleKey, mReplyKey;

    private final SavedStateHandle mSavedStateHandle;

    private final ReplyRepository mReplyRepository;

    private String mReply;

    public UpdateReplyViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mArticleId = savedStateHandle.get("artl_num");
        mReplyId = savedStateHandle.get("cmmt_num");
        mArticleKey = savedStateHandle.get("artl_key");
        mReplyKey = savedStateHandle.get("cmmt_key");
        mReply = savedStateHandle.get("cmt");
        mReplyRepository = new ReplyRepository(mGroupId, mArticleId, mArticleKey);

        if (mReply != null) {
            mReply = mReply.contains("※") ? mReply.substring(0, mReply.lastIndexOf("※")).trim() : mReply;
        }
    }

    public String getReply() {
        return mReply;
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ReplyFormState> getReplyFormState() {
        return mSavedStateHandle.getLiveData(REPLY_FORM_STATE);
    }

    public void actionSend(String text) {
        if (!TextUtils.isEmpty(text)) {
            mReplyRepository.setReply(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mReplyId, mReplyKey, text, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mSavedStateHandle.set(STATE, new State(false, data.toString(), null));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    mSavedStateHandle.set(STATE, new State(false, null, throwable.getMessage()));
                }

                @Override
                public void onLoading() {
                    mSavedStateHandle.set(STATE, new State(true, null, null));
                }
            });
        } else {
            mSavedStateHandle.set(REPLY_FORM_STATE, new ReplyFormState("내용을 입력하세요."));
        }
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public String text;

        public String message;

        public State(boolean isLoading, String text, String message) {
            this.isLoading = isLoading;
            this.text = text;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            text = in.readString();
            message = in.readString();
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (isLoading ? 1 : 0));
            parcel.writeString(text);
            parcel.writeString(message);
        }
    }

    public static final class ReplyFormState implements Parcelable {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }

        protected ReplyFormState(Parcel in) {
            replyError = in.readString();
        }

        public static final Creator<ReplyFormState> CREATOR = new Creator<ReplyFormState>() {
            @Override
            public ReplyFormState createFromParcel(Parcel in) {
                return new ReplyFormState(in);
            }

            @Override
            public ReplyFormState[] newArray(int size) {
                return new ReplyFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(replyError);
        }
    }
}
