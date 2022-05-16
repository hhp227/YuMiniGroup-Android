package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class GroupViewModel extends ViewModel {
    public boolean isAdmin;

    public int mPosition;

    public String mGroupId, mGroupName, mGroupImage, mKey;

    public GroupViewModel(SavedStateHandle savedStateHandle) {
        isAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("grp_img");
        mPosition = savedStateHandle.get("pos");
        mKey = savedStateHandle.get("key");
    }
}
