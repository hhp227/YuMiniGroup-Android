package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PictureViewModel extends ViewModel {
    private final SavedStateHandle mSavedStateHandle;

    public void setPosition(int position) {
        mSavedStateHandle.set("position", position);
    }

    public LiveData<Integer> getPosition() {
        return mSavedStateHandle.getLiveData("position");
    }

    public LiveData<List<String>> getImageList() {
        return mSavedStateHandle.getLiveData("images");
    }

    public PictureViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
    }
}