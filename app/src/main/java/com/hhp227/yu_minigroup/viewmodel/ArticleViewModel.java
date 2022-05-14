package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class ArticleViewModel extends ViewModel {
    public final MutableLiveData<String> test = new MutableLiveData<>();

    private SavedStateHandle mSavedStateHandle;

    public ArticleViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
        Log.e("TEST", "ArticleViewModel init");
        Log.e("TEST", "???" + savedStateHandle.get("grp_id"));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "ArticleViewModel onCleared");
    }
}
