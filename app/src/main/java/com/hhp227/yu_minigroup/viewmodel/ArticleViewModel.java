package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
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

    private static final String STATE = "state", REPLY_FORM_STATE = "replyFormState", UPDATE_ARTICLE_STATE = "updateArticleState", ARTICLE = "article";

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

        if (!mSavedStateHandle.contains(STATE)) {
            mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, null));
            fetchArticleData(mArticleId);
        }
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public void setState(State state) {
        mSavedStateHandle.set(STATE, state);
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ReplyFormState> getReplyFormState() {
        return mSavedStateHandle.getLiveData(REPLY_FORM_STATE);
    }

    public void setUpdateArticleState(boolean bool) {
        mSavedStateHandle.set(UPDATE_ARTICLE_STATE, bool);
    }

    // TODO ArticleState가 업데이트되면 변경되는걸로 변경하기
    public LiveData<Boolean> getUpdateArticleState() {
        return mSavedStateHandle.getLiveData(UPDATE_ARTICLE_STATE);
    }

    public void setArticleState(ArticleItem articleItem) {
        mSavedStateHandle.set(ARTICLE, articleItem);
    }

    public LiveData<ArticleItem> getArticleState() {
        return mSavedStateHandle.getLiveData(ARTICLE);
    }

    public void setScrollToLastState(boolean bool) {
        mSavedStateHandle.set("isbottom", bool);
    }

    public LiveData<Boolean> getScrollToLastState() {
        return mSavedStateHandle.getLiveData("isbottom");
    }

    public void actionSend(String text) {
        if (text.length() > 0) {
            mReplyRepository.addReply(getCookie(), mPreferenceManager.getUser(), text, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    refreshReply((List<Element>) data);

                    // 전송할때마다 리스트뷰 아래로
                    setScrollToLastState(true);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
                }

                @Override
                public void onLoading() {
                    State state = mSavedStateHandle.get(STATE);

                    if (state != null) {
                        setState(new State(true, state.articleItem, Collections.emptyList(), false, null));
                    }
                }
            });
        } else {
            mSavedStateHandle.set(REPLY_FORM_STATE, new ReplyFormState("댓글을 입력하세요."));
        }
    }

    public void deleteArticle() {
        mArticleRepository.removeArticle(getCookie(), mArticleId, mArticleKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setState(new State(false, null, Collections.emptyList(), true, "삭제완료"));
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                setState(new State(true, null, Collections.emptyList(), false, null));
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
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                State state = mSavedStateHandle.get(STATE);

                if (state != null) {
                    setState(new State(true, state.articleItem, state.replyItemList, false, null));
                }
            }
        });
    }

    public void refresh() {
        fetchArticleData(mArticleId);
    }

    public void refreshReply(List<Element> commentList) {
        fetchReplyData(commentList);
    }

    private void fetchArticleData(String articleId) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";

        mArticleRepository.getArticleData(getCookie(), articleId, mArticleKey, params, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                State state = mSavedStateHandle.get(STATE);

                if (data instanceof ArticleItem) {
                    ArticleItem articleItem = (ArticleItem) data;

                    if (state != null) {
                        setState(new State(false, articleItem, state.replyItemList, false, null));
                    }
                } else {
                    refreshReply((List<Element>) data);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                State state = mSavedStateHandle.get(STATE);

                if (state != null) {
                    setState(new State(true, null, state.replyItemList, false, null));
                }
            }
        });
    }

    private void fetchReplyData(List<Element> commentList) {
        mReplyRepository.getReplyList(commentList, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setState(new State(false, null, (List<Map.Entry<String, ReplyItem>>) data, false, null));
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
            }
        });
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public ArticleItem articleItem;

        public List<Map.Entry<String, ReplyItem>> replyItemList;

        public boolean isSetResultOK;

        public String message;

        public State(boolean isLoading, ArticleItem articleItem, List<Map.Entry<String, ReplyItem>> replyItemList, boolean isSetResultOK, String message) {
            this.isLoading = isLoading;
            this.articleItem = articleItem;
            this.replyItemList = replyItemList;
            this.isSetResultOK = isSetResultOK;
            this.message = message;
        }

        private State(Parcel in) {
            isLoading = in.readByte() != 0;
            isSetResultOK = in.readByte() != 0;
            message = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isLoading ? 1 : 0));
            dest.writeByte((byte) (isSetResultOK ? 1 : 0));
            dest.writeString(message);
        }

        @Override
        public int describeContents() {
            return 0;
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
    }

    public static final class ReplyFormState implements Parcelable {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }

        private ReplyFormState(Parcel in) {
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
