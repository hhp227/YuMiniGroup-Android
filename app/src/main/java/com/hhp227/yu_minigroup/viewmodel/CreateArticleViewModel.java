package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

public class CreateArticleViewModel extends ViewModel {

    public CreateArticleViewModel() {
        Log.e("TEST", "CreateArticleViewModel init");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "CreateArticleViewModel onCleared");
    }
}
