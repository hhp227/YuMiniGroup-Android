package com.hhp227.yu_minigroup.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.hhp227.yu_minigroup.dto.User;

import java.util.Set;

public class PreferenceManager {
    private static final String TAG = "세션메니져";

    // SharedPreference 파일 이름
    private static final String PREF_NAME = "ApplicationLogin";

    private static final String KEY_USER_ID = "usr_id";
    private static final String KEY_USER_PASSWORD = "usr_pwd";
    private static final String KEY_COOKIE = "cookie";
    private static final String KEY_SSO_TOKEN = "ssotoken";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    final int PRIVATE_MOD = 0;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MOD);
        editor = sharedPreferences.edit();
    }

    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_PASSWORD, user.getPassword());
        editor.commit();

        Log.i(TAG, "사용자 Session 저장 : " + user.getUserId());
    }

    public User getUser() {
        if (sharedPreferences.getString(KEY_USER_ID, null) != null) {
            String knuId = sharedPreferences.getString(KEY_USER_ID, null);
            String password = sharedPreferences.getString(KEY_USER_PASSWORD, null);
            User user = new User(knuId, password);

            return user;
        }
        return null;
    }

    public void storeToken(String token) {
        editor.putString(KEY_SSO_TOKEN, token);
        editor.commit();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_SSO_TOKEN, null);
    }

    public void storeCookie(String cookie) {
        editor.putString(KEY_COOKIE, cookie);
        editor.commit();
    }

    public String getCookie() {
        return sharedPreferences.getString(KEY_COOKIE, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
