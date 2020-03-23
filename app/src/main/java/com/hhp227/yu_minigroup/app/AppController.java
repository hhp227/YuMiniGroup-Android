package com.hhp227.yu_minigroup.app;

import android.app.Application;
import android.text.TextUtils;
import android.webkit.CookieManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    private CookieManager mCookieManager;
    private RequestQueue mRequestQueue;
    private PreferenceManager mPreferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public PreferenceManager getPreferenceManager() {
        if (mPreferenceManager == null) {
            mPreferenceManager = new PreferenceManager(this);
        }
        return mPreferenceManager;
    }

    public CookieManager getCookieManager() {
        if (mCookieManager == null)
            mCookieManager = CookieManager.getInstance();
        return mCookieManager;
    }

    public void setCookieManager(CookieManager cookieManager) {
        this.mCookieManager = cookieManager;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
