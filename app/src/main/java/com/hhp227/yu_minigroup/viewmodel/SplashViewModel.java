package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.SSLConnect;

import java.util.HashMap;
import java.util.Map;

public class SplashViewModel extends ViewModel {
    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<Boolean> mSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    public MutableLiveData<Boolean> isSuccess() {
        return mSuccess;
    }

    public MutableLiveData<String> getMessage() {
        return mMessage;
    }

    public void loginLMS(String ssoToken, String lmsToken) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            if (ssoToken != null) {
                mSuccess.postValue(true);
            }
        }, error -> {
            VolleyLog.e(SplashViewModel.class.getSimpleName(), error.getMessage());
            mSuccess.postValue(false);
            mMessage.postValue(error.getMessage());
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.allHeaders != null) {
                    response.allHeaders.stream()
                            .filter(header -> header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_IMAX"))
                            .forEach(header -> loginSSOyuPortal(header.getValue()));
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // 리퀘스트 헤더에 SESSION_IMAX값이 있음
                headers.put("Cookie", lmsToken != null ? lmsToken + "; " + ssoToken : null);
                return headers;
            }
        }, "req_login_LMS");
    }

    private void loginSSOyuPortal(String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.YU_PORTAL_LOGIN_URL, response -> {
            VolleyLog.d(SplashViewModel.class.getSimpleName(), "로그인 응답 : " + response);
            mCookieManager.setCookie(EndPoint.LOGIN_LMS, cookie);
        }, error -> {
            VolleyLog.e(SplashViewModel.class.getSimpleName(), "로그인 에러 : " + error.getMessage());
            mSuccess.postValue(false);
            mMessage.postValue(error.getMessage());
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.allHeaders != null) {
                    response.allHeaders.stream()
                            .filter(header -> header.getName().equals("Set-Cookie") && header.getValue().contains("ssotoken"))
                            .forEach(header -> loginLMS(header.getValue(), cookie));
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Referer", "http://portal.yu.ac.kr/sso/login.jsp"); // 필수
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                User user = mPreferenceManager.getUser();
                String id = user.getUserId();
                String password = user.getPassword();
                Map<String, String> params = new HashMap<>();

                params.put("cReturn_Url", EndPoint.LOGIN_LMS);
                params.put("type", "lms"); // 필수
                params.put("p", "20112030550005B055003090F570256534A010F47070C4556045E18020750110"); // 필수
                params.put("login_gb", "0"); // 필수
                params.put("userId", id);
                params.put("password", password);
                return params;
            }
        };

        new SSLConnect().postHttps(EndPoint.YU_PORTAL_LOGIN_URL, 1000, 1000);
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }
}