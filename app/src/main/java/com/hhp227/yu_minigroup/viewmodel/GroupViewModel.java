package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class GroupViewModel extends ViewModel {
    private SavedStateHandle mSavedStateHandle;

    public GroupViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
    }
}
