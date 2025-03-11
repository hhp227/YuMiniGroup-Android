package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileViewModel extends ViewModel {
    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> mSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<User> mUser = new MutableLiveData<>(mPreferenceManager.getUser());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<Boolean> isSuccess() {
        return mSuccess;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
    }

    public LiveData<Bitmap> getBitmap() {
        return mBitmap;
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public void sync() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoint.SYNC_PROFILE, null, response -> {
            try {
                if (!response.getBoolean("isError")) {
                    mLoading.postValue(false);
                    mSuccess.postValue(true);
                    mUser.postValue(mPreferenceManager.getUser());
                    mMessage.postValue(response.getString("message"));
                } else {
                    mLoading.postValue(false);
                    mMessage.postValue("동기화 실패");
                }
            } catch (JSONException e) {
                mLoading.postValue(false);
                mMessage.postValue(e.getMessage());
            }
        }, error -> {
            mLoading.postValue(false);
            mMessage.postValue(error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }
        };

        mLoading.postValue(true);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void uploadImage(boolean isUpdate) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, isUpdate ? EndPoint.PROFILE_IMAGE_UPDATE : EndPoint.PROFILE_IMAGE_PREVIEW, response -> {
            if (isUpdate) {
                mLoading.postValue(false);
                mSuccess.postValue(true);
                mUser.postValue(mPreferenceManager.getUser());
                mMessage.postValue(new String(response.data).contains("성공") ? "수정되었습니다." : "실패했습니다.");
            } else {
                uploadImage(true);
            }
        }, error -> {
            mLoading.postValue(false);
            mMessage.postValue(error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("FLAG", "FILE");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                Bitmap bitmap = mBitmap.getValue();

                if (bitmap != null) {
                    params.put("img_file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(bitmap)));
                }
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        mLoading.postValue(true);
        AppController.getInstance().addToRequestQueue(multipartRequest);
    }
}
