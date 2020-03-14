package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hhp227.yu_minigroup.CreateActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.hhp227.yu_minigroup.CreateActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "프로필";
    private boolean mIsVisible;
    private Bitmap mBitmap;
    private ImageView mProfileImage;
    private PreferenceManager mPreferenceManager;
    private Snackbar mProgressSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView name = findViewById(R.id.tv_name);
        TextView yuId = findViewById(R.id.tv_yu_id);
        TextView department = findViewById(R.id.tv_dept);
        TextView grade = findViewById(R.id.tv_grade);
        TextView email = findViewById(R.id.tv_email);
        TextView hp = findViewById(R.id.tv_phone_num);
        Button sync = findViewById(R.id.b_sync);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        User user = mPreferenceManager.getUser();
        mProfileImage = findViewById(R.id.iv_profile_image);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mPreferenceManager.getCookie()).build()))
                .apply(RequestOptions
                        .errorOf(R.drawable.user_image_view)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mProfileImage);
        mProfileImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            openContextMenu(v);
            unregisterForContextMenu(v);
        });
        sync.setOnClickListener(v -> {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoint.SYNC_PROFILE, null, response -> {
                hideProgressBar();
                try {
                    if (!response.getBoolean("isError")) {
                        Glide.with(getApplicationContext())
                                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mPreferenceManager.getCookie()).build()))
                                .apply(RequestOptions
                                        .errorOf(R.drawable.user_image_view_circle)
                                        .circleCrop()
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(mProfileImage);
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

                    headers.put("Cookie", mPreferenceManager.getCookie());
                    return headers;
                }
            };

            showProgressBar();
            AppController.getInstance().addToRequestQueue(jsonObjectRequest);
        });
        name.setText(user.getName());
        yuId.setText(user.getUserId());
        department.setText(user.getDepartment());
        grade.setText(user.getGrade());
        email.setText(user.getEmail());
        hp.setText(user.getPhoneNumber());
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
                    .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                    .into(mProfileImage);
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_send);

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

                headers.put("Cookie", mPreferenceManager.getCookie());
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
