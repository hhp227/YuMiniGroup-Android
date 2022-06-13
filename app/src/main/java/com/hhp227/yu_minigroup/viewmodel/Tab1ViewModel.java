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
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Tab1ViewModel extends ViewModel {
    public static Boolean mIsAdmin;

    public static String mGroupId, mGroupName, mGroupImage, mKey;

    private static final int LIMIT = 10;

    private static final String STATE = "state";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private final ArticleRepository articleRepository;

    public Tab1ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mIsAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("grp_img");
        mKey = savedStateHandle.get("key");
        articleRepository = new ArticleRepository(mGroupId, mKey);

        if (!mSavedStateHandle.contains(STATE)) {
            setState(new State(false, Collections.emptyList(), 1, false, false, null));
            fetchNextPage();
        }
    }

    private void setState(State state) {
        mSavedStateHandle.set(STATE, state);
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void fetchArticleList(int offset) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + offset + "&displayL=" + LIMIT;

        articleRepository.getArticleList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), params, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                State state = mSavedStateHandle.get(STATE);
                List<Map.Entry<String, ArticleItem>> articleItemList = (List<Map.Entry<String, ArticleItem>>) data;

                if (state != null) {
                    setState(new State(false, mergedList(state.articleItemList, articleItemList), state.offset + LIMIT, false, articleItemList.isEmpty(), null));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, Collections.emptyList(), 1, false, false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                State state = mSavedStateHandle.get(STATE);

                if (state != null) {
                    setState(new State(true, state.articleItemList, offset, offset > 1, false, null));
                }
            }
        });
    }

    public void fetchNextPage() {
        State state = mSavedStateHandle.get(STATE);

        if (state != null && !articleRepository.isStopRequestMore()) {
            setState(new State(false, state.articleItemList, state.offset, true, false, null));
        }
    }

    public void refresh() {
        articleRepository.setMinId(0);
        setState(new State(false, Collections.emptyList(), 1, true, false, null));
    }

    public void updateArticleItem(int position, AbstractMap.SimpleEntry<String, ArticleItem> kvSimpleEntry) {
        State state = mSavedStateHandle.get(STATE);

        if (state != null) {
            state.articleItemList.set(position, kvSimpleEntry);
            setState(state);
        }
    }

    private List<Map.Entry<String, ArticleItem>> mergedList(List<Map.Entry<String, ArticleItem>> existingList, List<Map.Entry<String, ArticleItem>> newList) {
        List<Map.Entry<String, ArticleItem>> result = new ArrayList<>();

        result.addAll(existingList);
        result.addAll(newList);
        return result;
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public List<Map.Entry<String, ArticleItem>> articleItemList;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, ArticleItem>> articleItemList, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.articleItemList = articleItemList;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }

        private State(Parcel in) {
            isLoading = in.readByte() != 0;
            offset = in.readInt();
            hasRequestedMore = in.readByte() != 0;
            isEndReached = in.readByte() != 0;
            message = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isLoading ? 1 : 0));
            dest.writeInt(offset);
            dest.writeByte((byte) (hasRequestedMore ? 1 : 0));
            dest.writeByte((byte) (isEndReached ? 1 : 0));
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
}
