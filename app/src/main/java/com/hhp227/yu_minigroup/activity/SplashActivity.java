package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.SSLConnect;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 1250;

    private static final String TAG = SplashActivity.class.getSimpleName();

    private PreferenceManager mPreferenceManager;

    private ActivitySplashBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySplashBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        Handler handler = new Handler(getMainLooper());
        Window window = getWindow();
        mPreferenceManager = AppController.getInstance().getPreferenceManager();

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        handler.postDelayed(() -> {
            User user = mPreferenceManager.getUser();

            loginLMS(user.getUserId(), user.getPassword(), null, null);
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void loginLMS(String id, String password, String ssoToken, String lmsToken) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            if (ssoToken != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
                overridePendingTransition(com.hhp227.yu_minigroup.R.anim.splash_in, R.anim.splash_out);
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                response.allHeaders.stream()
                        .filter(header -> header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_IMAX"))
                        .forEach(header -> loginSSOyuPortal(id, password, header.getValue()));
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

    private void loginSSOyuPortal(String id, String password, String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.YU_PORTAL_LOGIN_URL, response -> {
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            AppController.getInstance().getCookieManager().setCookie(EndPoint.LOGIN_LMS, cookie);
        }, error -> {
            VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                response.allHeaders.stream()
                        .filter(header -> header.getName().equals("Set-Cookie") && header.getValue().contains("ssotoken"))
                        .forEach(header -> loginLMS(id, password, header.getValue(), cookie));
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
