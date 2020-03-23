package com.hhp227.yu_minigroup.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.hhp227.yu_minigroup.dto.User;

public class PreferenceManager {
    private static final String TAG = "세션메니져";

    // SharedPreference 파일 이름
    private static final String PREF_NAME = "ApplicationLogin";

    private static final String KEY_USER_ID = "usr_id";
    private static final String KEY_USER_PASSWORD = "usr_pwd";
    private static final String KEY_USER_NAME = "usr_nm";
    private static final String KEY_USER_DEPT_NAME = "usr_dept_nm";
    private static final String KEY_USER_NUMBER = "usr_stu_id";
    private static final String KEY_USER_GRADE = "usr_grade";
    private static final String KEY_USER_EMAIL = "usr_mail";
    private static final String KEY_USER_UNIQUE_ID = "usr_uid";
    private static final String KEY_USER_HP = "usr_hp";
    private static final String KEY_SSO_TOKEN = "ssotoken";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    final int PRIVATE_MOD = 0;

    public PreferenceManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MOD);
        mEditor = mSharedPreferences.edit();
    }

    public void storeUser(User user) {
        mEditor.putString(KEY_USER_ID, user.getUserId());
        mEditor.putString(KEY_USER_PASSWORD, user.getPassword());
        mEditor.putString(KEY_USER_NAME, user.getName());
        mEditor.putString(KEY_USER_DEPT_NAME, user.getDepartment());
        mEditor.putString(KEY_USER_NUMBER, user.getNumber());
        mEditor.putString(KEY_USER_GRADE, user.getGrade());
        mEditor.putString(KEY_USER_EMAIL, user.getEmail());
        mEditor.putString(KEY_USER_UNIQUE_ID, user.getUid());
        mEditor.putString(KEY_USER_HP, user.getPhoneNumber());
        mEditor.commit();

        Log.i(TAG, "사용자 Session 저장 : " + user.getUserId());
    }

    public User getUser() {
        if (mSharedPreferences.getString(KEY_USER_ID, null) != null) {
            String yuId = mSharedPreferences.getString(KEY_USER_ID, null);
            String password = mSharedPreferences.getString(KEY_USER_PASSWORD, null);
            String name = mSharedPreferences.getString(KEY_USER_NAME, null);
            String deptName = mSharedPreferences.getString(KEY_USER_DEPT_NAME, null);
            String number = mSharedPreferences.getString(KEY_USER_NUMBER, null);
            String grade = mSharedPreferences.getString(KEY_USER_GRADE, null);
            String email = mSharedPreferences.getString(KEY_USER_EMAIL, null);
            String uid = mSharedPreferences.getString(KEY_USER_UNIQUE_ID, null);
            String hp = mSharedPreferences.getString(KEY_USER_HP, null);
            User user = new User(yuId, password, name, deptName, number, grade, email, uid, hp);

            return user;
        }
        return null;
    }

    public void storeToken(String token) {
        mEditor.putString(KEY_SSO_TOKEN, token);
        mEditor.commit();
    }

    public String getToken() {
        return mSharedPreferences.getString(KEY_SSO_TOKEN, null);
    }

    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }
}
