package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;
import java.util.List;

public class ListViewModel<T> extends ViewModel {
    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<T>> mItemList = new MutableLiveData<>(Collections.emptyList());

    private int offset = 1;

    private final MutableLiveData<Boolean> mRequestMore = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> mEndReached = new MutableLiveData<>(false);

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    public void setLoading(boolean bool) {
        mLoading.postValue(bool);
    }

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public void setItemList(List<T> list) {
        mItemList.postValue(list);
    }

    public LiveData<List<T>> getItemList() {
        return mItemList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int value) {
        offset = value;
    }

    public void setRequestMore(boolean bool) {
        mRequestMore.postValue(bool);
    }

    public LiveData<Boolean> hasRequestMore() {
        return mRequestMore;
    }

    public void setEndReached(boolean bool) {
        mEndReached.postValue(bool);
    }

    public LiveData<Boolean> isEndReached() {
        return mEndReached;
    }

    public void setMessage(String message) {
        mMessage.postValue(message);
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }
}