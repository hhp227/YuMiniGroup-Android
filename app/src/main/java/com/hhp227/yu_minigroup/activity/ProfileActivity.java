package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.*;
import android.webkit.CookieManager;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hhp227.yu_minigroup.activity.CreateActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.hhp227.yu_minigroup.activity.CreateActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "프로필";

    private boolean mIsVisible;

    private Bitmap mBitmap;

    private CookieManager mCookieManager;

    private Snackbar mProgressSnackBar;

    private ActivityProfileBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfileBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mCookieManager = AppController.getInstance().getCookieManager();
        User user = AppController.getInstance().getPreferenceManager().getUser();

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS))
                        .build()))
                .apply(RequestOptions
                        .errorOf(com.hhp227.yu_minigroup.R.drawable.user_image_view)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mBinding.ivProfileImage);
        mBinding.ivProfileImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            openContextMenu(v);
            unregisterForContextMenu(v);
        });
        mBinding.bSync.setOnClickListener(v -> {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoint.SYNC_PROFILE, null, response -> {
                hideProgressBar();
                try {
                    if (!response.getBoolean("isError")) {
                        Glide.with(getApplicationContext())
                                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder()
                                        .addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS))
                                        .build()))
                                .apply(RequestOptions
                                        .errorOf(com.hhp227.yu_minigroup.R.drawable.user_image_view_circle)
                                        .circleCrop()
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(mBinding.ivProfileImage);
                        setResult(RESULT_OK);
                        Snackbar.make(findViewById(android.R.id.content), response.getString("message"), Snackbar.LENGTH_LONG).show();
                    } else
                        Snackbar.make(findViewById(android.R.id.content), "동기화 실패", Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressBar();
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                    return headers;
                }
            };

            showProgressBar();
            AppController.getInstance().addToRequestQueue(jsonObjectRequest);
        });
        mBinding.tvName.setText(user.getName());
        mBinding.tvYuId.setText(user.getUserId());
        mBinding.tvDept.setText(user.getDepartment());
        mBinding.tvGrade.setText(user.getGrade());
        mBinding.tvEmail.setText(user.getEmail());
        mBinding.tvPhoneNum.setText(user.getPhoneNumber());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("프로필 이미지 변경");
        menu.add(Menu.NONE, 0, Menu.NONE, "앨범");
        menu.add(Menu.NONE, 1, Menu.NONE, "카메라");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case 0:
                intent = new Intent(Intent.ACTION_PICK);

                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                return true;
            case 1:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            mIsVisible = true;

            switch (requestCode) {
                case CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                    mBitmap = (Bitmap) data.getExtras().get("data");
                    break;
                case CAMERA_PICK_IMAGE_REQUEST_CODE:
                    mBitmap = new BitmapUtil(this).bitmapResize(data.getData(), 200);
                    break;
            }
            Glide.with(getApplicationContext())
                    .load(mBitmap)
                    .apply(RequestOptions.errorOf(com.hhp227.yu_minigroup.R.drawable.user_image_view_circle).circleCrop())
                    .into(mBinding.ivProfileImage);
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.hhp227.yu_minigroup.R.menu.modify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(com.hhp227.yu_minigroup.R.id.action_send);

        menuItem.setVisible(mIsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                uploadImage(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadImage(boolean isUpdate) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, isUpdate ? EndPoint.PROFILE_IMAGE_UPDATE : EndPoint.PROFILE_IMAGE_PREVIEW, response -> {
            if (isUpdate) {
                hideProgressBar();
                setResult(RESULT_OK);
                Snackbar.make(findViewById(android.R.id.content), new String(response.data).contains("성공") ? "수정되었습니다." : "실패했습니다.", Snackbar.LENGTH_LONG).show();
            } else
                uploadImage(true);
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
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

                params.put("img_file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(mBitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        showProgressBar();
        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    private void setProgressBar() {
        mProgressSnackBar = Snackbar.make(findViewById(android.R.id.content), "전송중...", Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) mProgressSnackBar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();

        contentLay.addView(new ProgressBar(getApplicationContext()));
    }

    private void showProgressBar() {
        setProgressBar();
        if (!mProgressSnackBar.isShown())
            mProgressSnackBar.show();
    }

    private void hideProgressBar() {
        if (mProgressSnackBar.isShown())
            mProgressSnackBar.dismiss();
    }
}
