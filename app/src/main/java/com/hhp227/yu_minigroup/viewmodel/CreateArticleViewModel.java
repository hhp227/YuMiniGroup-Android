package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.activity.CreateArticleActivity;

import java.util.ArrayList;
import java.util.List;

public class CreateArticleViewModel extends ViewModel {
    public final List<Object> mContents = new ArrayList<>();

    private static final String TAG = CreateArticleViewModel.class.getSimpleName(), BITMAP = "bitmap";

    private final SavedStateHandle mSavedStateHandle;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
    }

    public void setBitmap(Bitmap bitmap) {
        mSavedStateHandle.set(BITMAP, bitmap);
    }

    public LiveData<Bitmap> getBitmapState() {
        return mSavedStateHandle.getLiveData(BITMAP);
    }

    public <T> void addItem(T content) {
        mContents.add(content);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "CreateArticleViewModel onCleared");
    }
}
