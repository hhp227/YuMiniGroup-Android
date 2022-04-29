package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.SSLConnect;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginViewModel extends ViewModel {
    public MutableLiveData<State> mState = new MutableLiveData<>();

    private static final String TAG = "로그인화면";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public void login(String id, String password) {
        if (!id.isEmpty() && !password.isEmpty()) {
            mState.postValue(new State(true, null, null, null));
            if (id.equals("22000000") && password.equals("TestUser")) {
                firebaseLogin(id, password);
            } else {
                loginLMS(id, password, null, null);
            }
        } else {
            mState.postValue(new State(false, null, new LoginFormState(id.isEmpty() ? "아이디 또는 학번을 입력하세요." : null, password.isEmpty() ? "패스워드를 입력하세요." : null), null));
        }
    }

    public void storeUser(User user) {
        mPreferenceManager.storeUser(user);
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    private void loginLMS(String id, String password, String ssoToken, String lmsToken) {
        String tagStringReq = "req_login_LMS";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN_LMS, response -> {
            if (ssoToken != null) {
                getUserInfo(id, password);
            } else {
                mState.postValue(new State(false, null, null, "ssoToken is null"));
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mState.postValue(new State(false, null, null, "로그인 실패"));
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

    private void loginSSOyuPortal(String id, String password, String cookie) {
        String tagStringReq = "req_login_SSO";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.YU_PORTAL_LOGIN_URL, response -> {
            // TODO 로그인 성공/실패에 대한 처리 분기 필요
            VolleyLog.d(TAG, "로그인 응답 : " + response);
            mCookieManager.setCookie(EndPoint.LOGIN_LMS, cookie);
        }, error -> {
            VolleyLog.e(TAG, "로그인 에러 : " + error.getMessage());
            mState.postValue(new State(false, null, null, error.getMessage()));
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
                mState.postValue(new State(false, null, null, e.getMessage()));
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mState.postValue(new State(false, null, null, error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
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

    private void getUserUniqueId(User user) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GET_USER_IMAGE, response -> {
            Source source = new Source(response);
            String imageUrl = source.getElementById("photo").getAttributeValue("src");
            String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&size"));

            user.setUid(uid);
            mState.postValue(new State(false, user, null, null));
        }, error -> {
            VolleyLog.e(error.getMessage());
            mState.postValue(new State(false, null, null, error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    private void firebaseLogin(String id, String password) {
        String email = "TestUser@yu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                User user = new User();

                user.setUid(firebaseUser.getUid());
                user.setUserId(id);
                user.setPassword(password);
                user.setName("TestUser");
                user.setNumber("22000000");
                user.setPhoneNumber("01000000000");
                user.setEmail(email);
                mCookieManager.setCookie(EndPoint.LOGIN_LMS, firebaseUser.getUid());
                mState.postValue(new State(false, user, null, null));
            }
        }).addOnFailureListener(e -> mState.postValue(new State(false, null, null, "Firebase error" + e.getMessage())));
    }

    private void firebaseRegister(String id, String password) {
        String email = "TestUser@yu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                User user = new User();
                databaseReference.child(firebaseUser.getUid()).setValue(firebaseUser);

                user.setUid(firebaseUser.getUid());
                user.setUserId(id);
                user.setPassword(password);
                user.setName("TestUser");
                user.setNumber("22000000");
                user.setPhoneNumber("01000000000");
                user.setEmail(email);
                mState.postValue(new State(false, user, null, null));
            }
        }).addOnFailureListener(e -> mState.postValue(new State(false, null, null, "Firebase error" + e.getMessage())));
    }

    public static final class State {
        public boolean isLoading;

        public User user;

        public LoginFormState loginFormState;

        public String message;

        public State(boolean isLoading, User user, LoginFormState loginFormState, String message) {
            this.isLoading = isLoading;
            this.user = user;
            this.loginFormState = loginFormState;
            this.message = message;
        }
    }

    public static final class LoginFormState {
        public String emailError;

        public String passwordError;

        public LoginFormState(String emailError, String passwordError) {
            this.emailError = emailError;
            this.passwordError = passwordError;
        }
    }
}
