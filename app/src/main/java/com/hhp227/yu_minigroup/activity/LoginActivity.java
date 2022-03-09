package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityLoginBinding;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.SSLConnect;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "로그인화면";

    private CookieManager mCookieManager;

    private PreferenceManager mPreferenceManager;

    private ActivityLoginBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookieManager = AppController.getInstance().getCookieManager();

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (mPreferenceManager.getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        mBinding.bLogin.setOnClickListener(v -> {
            String id = mBinding.etId.getText().toString();
            String password = mBinding.etPassword.getText().toString();

            if (!id.isEmpty() && !password.isEmpty()) {
                showProgressBar();
                loginLMS(id, password, null, null);
            } else {
                mBinding.etId.setError(id.isEmpty() ? "아이디 또는 학번을 입력하세요." : null);
                mBinding.etPassword.setError(password.isEmpty() ? "패스워드를 입력하세요." : null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void loginSSOyuPortal(String id, String password, String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.YU_PORTAL_LOGIN_URL, response -> {
            // TODO 로그인 성공/실패에 대한 처리 분기 필요
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            mCookieManager.setCookie(EndPoint.LOGIN_LMS, cookie);
        }, error -> {
            VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressBar();
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

    private void loginLMS(String id, String password, String ssoToken, String lmsToken) {
        String tagStringReq = "req_login_LMS";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            Log.d(TAG, "로그인 응답 : " + response);
            if (ssoToken != null) {
                getUserInfo(id, password);
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Snackbar.make(getCurrentFocus(), "로그인 실패", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            hideProgressBar();
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.allHeaders != null) {
                    response.allHeaders.stream()
                            .filter(header -> header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_IMAX"))
                            .forEach(header -> loginSSOyuPortal(id, password, header.getValue()));
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
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private String getTextNodeValue(Node node) {
        if (node != null && node.hasChildNodes())
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
                if (child.getNodeType() == 3)
                    return child.getNodeValue();
        return "";
    }

    private void createLog(final User user) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.CREATE_LOG, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                if (!jsonObject.getBoolean("error")) {

                    // 로그기록 성공
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> VolleyLog.e(TAG, error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("name", user.getName());
                params.put("user_id", user.getUserId());
                params.put("password", user.getPassword());
                params.put("student_number", user.getNumber());
                params.put("type", "영남대 소모임");
                return params;
            }
        });
    }

    private void getUserInfo(final String id, final String password) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MY_INFO, response -> {
            try {
                Source source = new Source(response);
                List<String> extractedList = new ArrayList<>();
                User user = new User();

                source.getElementById("content_text").getAllElements(HTMLElementName.TR).forEach(element -> {
                    if (element.getAllElements(HTMLElementName.TD).size() > 1)
                        extractedList.add(String.valueOf(element.getAllElements(HTMLElementName.TD).get(1).getTextExtractor()).split(" ")[0]);
                });
                user.setUserId(id);
                user.setPassword(password);
                user.setName(extractedList.get(0));
                user.setPhoneNumber(extractedList.get(1));
                user.setEmail(extractedList.get(2));
                createLog(user);
                getUserUniqueId(user);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "LMS에 문제가 생겼습니다.", Toast.LENGTH_LONG).show();
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), "에러 : " + error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    private void getUserUniqueId(User user) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GET_USER_IMAGE, response -> {
            Source source = new Source(response);
            String imageUrl = source.getElementById("photo").getAttributeValue("src");
            String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&size"));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            user.setUid(uid);
            mPreferenceManager.storeUser(user);

            // 화면이동
            startActivity(intent);
            finish();
            hideProgressBar();
        }, error -> {
            VolleyLog.e(error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    private void showProgressBar() {
        if (mBinding.pbLogin.getVisibility() == View.GONE)
            mBinding.pbLogin.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbLogin.getVisibility() == View.VISIBLE)
            mBinding.pbLogin.setVisibility(View.GONE);
    }
}
