package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class Tab4ViewModel extends ViewModel {
    public Boolean mIsAdmin;

    public String mGroupId, mGroupImage, mKey;

    private static final String TAG = Tab4ViewModel.class.getSimpleName(), LOADING = "loading", SUCCESS = "success", MESSAGE = "message";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private final GroupRepository mGroupRepository = new GroupRepository();

    public Tab4ViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
        mIsAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupImage = savedStateHandle.get("grp_img");
        mKey = savedStateHandle.get("key");
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public void setSuccess(boolean bool) {
        mSavedStateHandle.set(SUCCESS, bool);
    }

    public LiveData<Boolean> isSuccess() {
        return mSavedStateHandle.getLiveData(SUCCESS);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void deleteGroup() {
        mGroupRepository.removeGroup(getCookie(), getUser(), mIsAdmin, mGroupId, mKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                setSuccess((Boolean) data);
                setMessage("소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료");
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }
}
