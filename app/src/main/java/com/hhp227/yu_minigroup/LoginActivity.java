package com.hhp227.yu_minigroup;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "로그인화면";
    private Button login;
    private EditText inputId, inputPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.b_login);
        inputId = findViewById(R.id.et_id);
        inputPassword = findViewById(R.id.et_password);
        progressBar = findViewById(R.id.pb_login);

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(v -> {
            String id = inputId.getText().toString();
            String password = inputPassword.getText().toString();

            if (!id.isEmpty() && !password.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, response -> {
                    VolleyLog.d(TAG, "로그인 응답 : " + response);
                    Toast.makeText(getApplicationContext(), response.trim(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }, error -> {
                    VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }) {
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        List<Header> headers = response.allHeaders;
                        for (Header header : headers)
                            if (header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_NEWLMS"))
                                AppController.getInstance().getPreferenceManager().storeCookie(header.getValue());
                        return super.parseNetworkResponse(response);
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                    }

                    @Override
                    public byte[] getBody() {
                        Map<String, String> params = new HashMap<>();
                        params.put("_enpass_login_", "submit");
                        params.put("cReturn_Url", "http://lms.yu.ac.kr/ilos/lo/login_sso.acl");
                        params.put("type", "lms");
                        params.put("login_lan", "ko");
                        params.put("p", "401130207510658045003090F570256534E00005D1405505D1151020D14501F0");
                        params.put("userId", id);
                        params.put("password", password);
                        if (params != null && params.size() > 0) {
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
            } else
                Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG).show();
        });
    }
}
