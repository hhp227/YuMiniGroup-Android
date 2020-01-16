package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.material.button.MaterialButton;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "로그인화면";
    private EditText mInputId, mInputPassword;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login = findViewById(R.id.b_login);
        mInputId = findViewById(R.id.et_id);
        mInputPassword = findViewById(R.id.et_password);
        mProgressBar = findViewById(R.id.pb_login);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (mPreferenceManager.getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(v -> {
            String id = mInputId.getText().toString();
            String password = mInputPassword.getText().toString();

            if (!id.isEmpty() && !password.isEmpty()) {
                showProgressBar();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, response -> {
                    VolleyLog.d(TAG, "로그인 응답 : " + response);
                    try {
                        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
                        String code = getTextNodeValue(((Element) document.getElementsByTagName("neo").item(0)).getElementsByTagName("code").item(0));
                        if (code.equals("00")) {
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();
                            loginLMS(id, password, null, null);
                        } else {
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                            hideProgressBar();
                        }
                    } catch (IOException | SAXException | ParserConfigurationException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressBar();
                }) {
                    @Override
                    public byte[] getBody() {
                        Map<String, String> params = new HashMap<>();
                        params.put("usr_id", id);
                        params.put("usr_pw", password);
                        if (params.size() > 0) {
                            StringBuilder encodedParams = new StringBuilder();
                            try {
                                params.forEach((k, v) -> {
                                    try {
                                        encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                        encodedParams.append("=");
                                        encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                        encodedParams.append("&");
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                });
                                return encodedParams.toString().getBytes(getParamsEncoding());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                };
                AppController.getInstance().addToRequestQueue(stringRequest);
            } else {
                mInputId.setError(id.isEmpty() ? "아이디를 입력하세요." : null);
                mInputPassword.setError(password.isEmpty() ? "패스워드를 입력하세요." : null);
            }
        });
    }

    private void loginSSOyuPortal(String id, String password, String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://portal.yu.ac.kr/sso/login_process.jsp", response -> {
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            mPreferenceManager.storeCookie(cookie);
        }, error -> {
            VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressBar();
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                List<Header> headers = response.allHeaders;
                for (Header header : headers)
                    if (header.getName().equals("Set-Cookie") && header.getValue().contains("ssotoken"))
                        loginLMS(id, password, header.getValue(), cookie);
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
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void loginLMS(String id, String password, String ssoToken, String lmsToken) {
        String tagStringReq = "req_login_LMS";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            if (ssoToken != null) {
                User user = new User(id, password);

                mPreferenceManager.storeUser(user);
                // 화면이동
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                hideProgressBar();
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressBar();
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.e("테스트", response.allHeaders.toString());
                for (Header header : response.allHeaders)
                    if (header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_IMAX"))
                        loginSSOyuPortal(id, password, header.getValue());
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

    private void showProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }
}
