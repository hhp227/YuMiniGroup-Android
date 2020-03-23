package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;
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
                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, response -> {
                    VolleyLog.d(TAG, "로그인 응답 : " + response);
                    try {
                        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(response.getBytes("utf-8"))));
                        String code = getTextNodeValue(((Element) document.getElementsByTagName("neo").item(0)).getElementsByTagName("code").item(0));
                        if (code.equals("00")) {
                            Snackbar.make(getCurrentFocus(), "로그인 성공", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            loginLMS(id, password, null, null);
                        } else {
                            Snackbar.make(getCurrentFocus(), "로그인 실패", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
                        throw new RuntimeException();
                    }
                };

                showProgressBar();
                AppController.getInstance().addToRequestQueue(stringRequest);
            } else {
                mInputId.setError(id.isEmpty() ? "아이디 또는 학번을 입력하세요." : null);
                mInputPassword.setError(password.isEmpty() ? "패스워드를 입력하세요." : null);
            }
        });
    }

    private void loginSSOyuPortal(String id, String password, String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.YU_PORTAL_LOGIN_URL, response -> {
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

                headers.stream()
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
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void loginLMS(String id, String password, String ssoToken, String lmsToken) {
        String tagStringReq = "req_login_LMS";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            if (ssoToken != null)
                getUserInfo(id, password);
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressBar();
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
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.NEW_MESSAGE, null, response -> {
            try {
                if (!response.getBoolean("isError")) {
                    JSONObject param = response.getJSONObject("param");
                    String name = param.getString("session.origin_nm");
                    String department = param.getString("session.dept_nm");
                    String number = param.getString("session.stu_id");
                    String grade = param.getString("session.grade");
                    String email = param.getString("session.email");
                    String hp = param.getString("session.hp_no");
                    User user = new User();

                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(name);
                    user.setDepartment(department);
                    user.setNumber(number);
                    user.setGrade(grade);
                    user.setEmail(email);
                    user.setPhoneNumber(hp);
                    createLog(user);
                    getUserUniqueId(user);
                } else
                    Toast.makeText(getApplicationContext(), "에러 발생", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            Toast.makeText(getApplicationContext(), "에러 : " + error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mPreferenceManager.getCookie());
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

                headers.put("Cookie", mPreferenceManager.getCookie());
                return headers;
            }
        });
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
