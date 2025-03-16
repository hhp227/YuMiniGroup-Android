package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class UserViewModel extends ViewModel {
    public final String mUid, mName, mValue;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public UserViewModel(SavedStateHandle savedStateHandle) {
        mUid = savedStateHandle.get("uid");
        mName = savedStateHandle.get("name");
        mValue = savedStateHandle.get("value");
    }

    public boolean isAuth() {
        return mPreferenceManager.getUser().getUid().equals(mUid);
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }
}
