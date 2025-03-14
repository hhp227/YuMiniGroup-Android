package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    private static final String LOADING = "loading", OFFSET = "offset", REQUEST_MORE = "requestMore", END_REACHED = "endReached", MESSAGE = "message";

    private final MutableLiveData<List<Map.Entry<String, ArticleItem>>> mItemList = new MutableLiveData<>(Collections.emptyList());

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

        setLoading(false);
        setOffset(1);
        setRequestMore(false);
        setEndReached(false);
        fetchNextPage();
    }

    private void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    private void setItemList(List<Map.Entry<String, ArticleItem>> itemList) {
        mItemList.postValue(itemList);
    }

    public LiveData<List<Map.Entry<String, ArticleItem>>> getItemList() {
        return mItemList;
    }

    private void setOffset(int offset) {
        mSavedStateHandle.set(OFFSET, offset);
    }

    public int getOffset() {
        return mSavedStateHandle.get(OFFSET);
    }

    private void setRequestMore(boolean bool) {
        mSavedStateHandle.set(REQUEST_MORE, bool);
    }

    public LiveData<Boolean> hasRequestMore() {
        return mSavedStateHandle.getLiveData(REQUEST_MORE);
    }

    private void setEndReached(boolean bool) {
        mSavedStateHandle.set(END_REACHED, bool);
    }

    public LiveData<Boolean> isEndReached() {
        return mSavedStateHandle.getLiveData(END_REACHED);
    }

    private void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void fetchArticleList(int offset) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + offset + "&displayL=" + LIMIT;

        articleRepository.getArticleList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), params, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, ArticleItem>> articleItemList = (List<Map.Entry<String, ArticleItem>>) data;

                setLoading(false);
                setItemList(mergedList(getItemList().getValue(), articleItemList));
                setOffset(getOffset() + LIMIT);
                setEndReached(articleItemList.isEmpty());
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
                setRequestMore(offset > 1);
            }
        });
    }

    public void fetchNextPage() {
        setRequestMore(!articleRepository.isStopRequestMore());
        if (!articleRepository.isStopRequestMore()) {
            fetchArticleList(getOffset());
        }
    }

    public void refresh() {
        articleRepository.setMinId(0);
        setRequestMore(true);
        setItemList(Collections.emptyList());
        setOffset(1);
        setEndReached(false);
        fetchArticleList(getOffset());
    }

    public void updateArticleItem(int position, AbstractMap.SimpleEntry<String, ArticleItem> kvSimpleEntry) {
        List<Map.Entry<String, ArticleItem>> itemList = getItemList().getValue();

        if (itemList != null && !itemList.isEmpty()) {
            itemList.set(position, kvSimpleEntry);
            setItemList(itemList);
        }
    }

    private List<Map.Entry<String, ArticleItem>> mergedList(List<Map.Entry<String, ArticleItem>> existingList, List<Map.Entry<String, ArticleItem>> newList) {
        return new ArrayList<Map.Entry<String, ArticleItem>>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}