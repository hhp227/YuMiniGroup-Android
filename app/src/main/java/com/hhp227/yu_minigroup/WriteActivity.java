package com.hhp227.yu_minigroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.yu_minigroup.adapter.WriteListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.WriteItem;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WriteActivity extends AppCompatActivity {
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    private static final String TAG = WriteActivity.class.getSimpleName();
    private boolean mIsAdmin;
    private String mGrpId, mGrpNm, mCurrentPhotoPath, mCookie, mKey;
    private List<String> mImages;
    private List<WriteItem> mContents;
    private PreferenceManager mPreferenceManager;
    private ProgressDialog mProgressDialog;
    private RecyclerView mRecyclerView;
    private StringBuilder mMakeHtmlImages;
    private Uri mPhotoUri;
    private WriteListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout buttonImage = findViewById(R.id.ll_image);
        LinearLayout buttonVideo = findViewById(R.id.ll_video);
        mRecyclerView = findViewById(R.id.rv_write);
        mContents = new ArrayList<>();
        mAdapter = new WriteListAdapter(this, mContents);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookie = mPreferenceManager.getCookie();
        mProgressDialog = new ProgressDialog(this);
        mIsAdmin = getIntent().getBooleanExtra("admin", false);
        mGrpId = getIntent().getStringExtra("grp_id");
        mGrpNm = getIntent().getStringExtra("grp_nm");
        mKey = getIntent().getStringExtra("key");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAdapter.addHeaderView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        buttonImage.setOnClickListener(this::showContextMenu);
        buttonVideo.setOnClickListener(this::showContextMenu);
        mProgressDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                WriteListAdapter.HeaderHolder holder = mAdapter.getHeaderHolder();
                String title = holder.inputTitle.getEditableText().toString();
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml((Spanned) holder.inputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml((Spanned) holder.inputContent.getText());
                if (!title.isEmpty() && !(TextUtils.isEmpty(content) && mContents.size() < 2)) {
                    mMakeHtmlImages = new StringBuilder();
                    mImages = new ArrayList<>();
                    mProgressDialog.setMessage("전송중...");
                    mProgressDialog.setProgressStyle(mContents.size() > 1 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();

                    if (mContents.size() > 1) {
                        int position = 1;
                        uploadImage(position, mContents.get(position).getBitmap());
                    } else
                        actionSend(mGrpId, title, content);
                } else
                    Snackbar.make(getCurrentFocus(), (title.isEmpty() ? "제목" : "내용") + "을 입력하세요.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (v.getId()) {
            case R.id.ll_image:
                menu.setHeaderTitle("이미지 선택");
                menu.add(1, Menu.NONE, Menu.NONE, "갤러리");
                menu.add(2, Menu.NONE, Menu.NONE, "카메라");
                break;
            case R.id.ll_video:
                menu.setHeaderTitle("동영상 선택");
                menu.add(3, Menu.NONE, Menu.NONE, "동영상");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getGroupId()) {
            case 0:
                mContents.remove(item.getItemId());
                mAdapter.notifyItemRemoved(item.getItemId());
                return true;
            case 1:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                return true;
            case 2:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                return true;
            case 3:
                Toast.makeText(getApplicationContext(), "동영상 선택", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            bitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);

            WriteItem writeItem = new WriteItem();
            writeItem.setBitmap(bitmap);
            writeItem.setFileUri(fileUri);

            mContents.add(writeItem);
            mAdapter.notifyItemInserted(mContents.size() - 1);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                bitmap = new BitmapUtil(this).bitmapResize(mPhotoUri, 200);
                if (bitmap != null) {
                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    int angle = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90
                            : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                            : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270
                            : 0;
                    Bitmap rotatedBitmap = new BitmapUtil(this).rotateImage(bitmap, angle);
                    WriteItem writeItem = new WriteItem(mPhotoUri, rotatedBitmap, null);

                    mContents.add(writeItem);
                    mAdapter.notifyItemInserted(mContents.size() - 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(int position, Bitmap bitmap) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, response -> {
            int count = position;
            mProgressDialog.setProgress((int) ((double) (count) / mContents.size() * 100));
            try {
                String imageSrc = new String(response.data);
                imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles2/"), imageSrc.lastIndexOf("\""));
                mMakeHtmlImages.append("<p><img src=\"" + imageSrc + "\" width=\"488\"><p>" + (count < mContents.size() - 1 ? "<br>": ""));
                mImages.add(imageSrc);
                if (count < mContents.size() - 1) {
                    count++;
                    Thread.sleep(700);
                    uploadImage(count, mContents.get(count).getBitmap());
                } else {
                    WriteListAdapter.HeaderHolder holder = mAdapter.getHeaderHolder();
                    String title = holder.inputTitle.getEditableText().toString();
                    String content = (!TextUtils.isEmpty(holder.inputContent.getText()) ? Html.toHtml((Spanned) holder.inputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlImages.toString();
                    actionSend(mGrpId, title, content);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(getCurrentFocus(), "이미지 업로드 실패", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                hideProgressDialog();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            hideProgressDialog();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mCookie);
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };
        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private void actionSend(String grpId, String title, String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, response -> {
            hideProgressDialog();

            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean error = jsonObject.getBoolean("isError");
                if (!error) {
                    Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(WriteActivity.this, GroupActivity.class);
                    intent.putExtra("admin", mIsAdmin);
                    intent.putExtra("grp_id", grpId);
                    intent.putExtra("grp_nm", mGrpNm);
                    intent.putExtra("key", mKey);
                    // 이전 Activity 초기화
                    intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                Log.e(TAG, "에러 : " + e.getMessage());
            } finally {
                getArticleId();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog();
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
                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", grpId);
                params.put("TXT", content);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void getArticleId() {
        String params = "?CLUB_GRP_ID=" + mGrpId + "&displayL=1";
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);
            String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");
            insertArticleToFirebase(artlNum);
        }, error -> VolleyLog.e(error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", mCookie);
                return headers;
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showContextMenu(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
        unregisterForContextMenu(v);
    }

    private void insertArticleToFirebase(String artlNum) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();
        map.put("id", artlNum);
        map.put("uid", mPreferenceManager.getUser().getUid());
        map.put("name", mPreferenceManager.getUser().getName());
        map.put("title", mAdapter.getHeaderHolder().inputTitle.getText().toString());
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(mAdapter.getHeaderHolder().inputContent.getText().toString()) ? null : mAdapter.getHeaderHolder().inputContent.getText().toString());
        map.put("images", mImages);

        databaseReference.child(mKey).push().setValue(map);
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
