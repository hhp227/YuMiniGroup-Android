package com.hhp227.yu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public void logout() {
        mPreferenceManager.clear();
        mCookieManager.removeAllCookies(value -> Log.d(TAG, "onReceiveValue " + value));
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }
}
