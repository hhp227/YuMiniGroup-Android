package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateActivity extends AppCompatActivity {
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 200;

    private static final String TAG = CreateActivity.class.getSimpleName();
    private boolean mJoinTypeCheck;
    private String mCookie, mPushId;
    private Bitmap mBitmap;
    private EditText mGroupTitle, mGroupDescription;
    private ImageView mGroupImage, mResetTitle;
    private PreferenceManager mPreferenceManager;
    private RadioGroup mJoinType;
    private RelativeLayout mProgressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mGroupTitle = findViewById(R.id.et_title);
        mGroupDescription = findViewById(R.id.et_description);
        mResetTitle = findViewById(R.id.iv_reset);
        mGroupImage = findViewById(R.id.iv_group_image);
        mJoinType = findViewById(R.id.rg_jointype);
        mProgressLayout = findViewById(R.id.rl_progress);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookie = mPreferenceManager.getCookie();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGroupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mResetTitle.setImageResource(s.length() > 0 ? R.drawable.ic_clear_black_24dp : R.drawable.ic_clear_gray_24dp);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mResetTitle.setOnClickListener(v -> mGroupTitle.setText(""));
        mGroupImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            openContextMenu(v);
            unregisterForContextMenu(v);
        });
        mJoinType.setOnCheckedChangeListener((group, checkedId) -> mJoinTypeCheck = checkedId != R.id.rb_auto);
        mJoinType.check(R.id.rb_auto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                String title = mGroupTitle.getText().toString().trim();
                String description = mGroupDescription.getText().toString().trim();
                String join = !mJoinTypeCheck ? "0" : "1";
                if (!title.isEmpty() && !description.isEmpty()) {
                    showProgressLayout();
                    AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.CREATE_GROUP, null, response -> {
                        try {
                            if (!response.getBoolean("isError")) {
                                String groupId = response.getString("CLUB_GRP_ID").trim();
                                String groupName = response.getString("GRP_NM");
                                if (mBitmap != null)
                                    groupImageUpdate(groupId, groupName, description, join);
                                else {
                                    insertGroupToFirebase(groupId, groupName, description, join);
                                    createGroupSuccess(groupId, groupName);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                            hideProgressLayout();
                        }
                    }, error -> {
                        VolleyLog.e(error.getMessage());
                        hideProgressLayout();
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Cookie", mCookie);
                            return headers;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                        }

                        @Override
                        public byte[] getBody() {
                            Map<String, String> params = new HashMap<>();
                            params.put("GRP_NM", title);
                            params.put("TXT", description);
                            params.put("JOIN_DIV", join);
                            if (params.size() > 0) {
                                StringBuilder encodedParams = new StringBuilder();
                                try {
                                    params.forEach((k, v) -> {
                                        try {
                                            encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                            encodedParams.append('=');
                                            encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                            encodedParams.append("&");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    return encodedParams.toString().getBytes(getParamsEncoding());
                                } catch (UnsupportedEncodingException uee) {
                                    throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                                }
                            }
                            return super.getBody();
                        }
                    });
                } else {
                    mGroupTitle.setError(title.isEmpty() ? "그룹명을 입력하세요." : null);
                    if (description.isEmpty())
                        Snackbar.make(getCurrentFocus(), "그룹설명을 입력해주세요.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.select_image));
        menu.add(getString(R.string.camera));
        menu.add(getString(R.string.gallery));
        menu.add(getString(R.string.image_none));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "카메라":
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                break;
            case "갤러리":
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                break;
            case "이미지 없음":
                mGroupImage.setImageResource(R.drawable.add_photo);
                mBitmap = null;
                Snackbar.make(getCurrentFocus(), "이미지 없음 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mBitmap = (Bitmap) data.getExtras().get("data");
            mGroupImage.setImageBitmap(mBitmap);
        } else if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            mBitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);
            mGroupImage.setImageBitmap(mBitmap);
        }
    }

    private void createGroupSuccess(String groupId, String groupName) {
        Intent intent = new Intent(CreateActivity.this, GroupActivity.class);
        intent.putExtra("admin", true);
        intent.putExtra("grp_id", groupId);
        intent.putExtra("grp_nm", groupName);
        intent.putExtra("key", mPushId);
        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
        hideProgressLayout();
        Snackbar.make(getCurrentFocus(), "그룹이 생성되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private void groupImageUpdate(String clubGrpId, String grpNm, String txt, String joinDiv) {
        AppController.getInstance().addToRequestQueue(new MultipartRequest(Request.Method.POST, EndPoint.GROUP_IMAGE_UPDATE, response -> {
            insertGroupToFirebase(clubGrpId, grpNm, txt, joinDiv);
            createGroupSuccess(clubGrpId, grpNm);
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressLayout();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mCookie);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("CLUB_GRP_ID", clubGrpId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(mBitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        });
    }

    private void insertGroupToFirebase(String groupId, String groupName, String description, String joinType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Boolean> members = new HashMap<>();
        members.put(mPreferenceManager.getUser().getUid(), true);
        GroupItem groupItem = new GroupItem();
        groupItem.setId(groupId);
        groupItem.setTimestamp(System.currentTimeMillis());
        groupItem.setAuthor(mPreferenceManager.getUser().getName());
        groupItem.setAuthorUid(mPreferenceManager.getUser().getUid());
        groupItem.setImage(EndPoint.BASE_URL + (mBitmap != null ? "/ilosfiles2/club/photo/" + groupId.concat(".jpg") : "/ilos/images/community/share_nophoto.gif"));
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(joinType);
        groupItem.setMembers(members);
        groupItem.setMemberCount(members.size());

        mPushId = databaseReference.push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("Groups/" + mPushId, groupItem);
        childUpdates.put("UserGroupList/" + mPreferenceManager.getUser().getUid() + "/" + mPushId, true);
        databaseReference.updateChildren(childUpdates);
    }

    private void showProgressLayout() {
        if (mProgressLayout != null && mProgressLayout.getVisibility() == View.GONE)
            mProgressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressLayout() {
        if (mProgressLayout != null && mProgressLayout.getVisibility() == View.VISIBLE)
            mProgressLayout.setVisibility(View.GONE);
    }
}
