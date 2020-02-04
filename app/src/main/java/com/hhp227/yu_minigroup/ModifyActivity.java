package com.hhp227.yu_minigroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.adapter.WriteListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.WriteItem;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.hhp227.yu_minigroup.WriteActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;
import static com.hhp227.yu_minigroup.WriteActivity.REQUEST_IMAGE_CAPTURE;

public class ModifyActivity extends AppCompatActivity {
    private static final String TAG = ModifyActivity.class.getSimpleName();
    private String mGrpId, mArtlNum, mCurrentPhotoPath, mCookie, mTitle, mContent, mGrpKey, mArtlKey;
    private List<String> mImageList;
    private List<WriteItem> mContents;
    private ProgressDialog mProgressDialog;
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
        RecyclerView recyclerView = findViewById(R.id.rv_write);
        Intent intent = getIntent();
        mContents = new ArrayList<>();
        mAdapter = new WriteListAdapter(this, mContents);
        mCookie = AppController.getInstance().getPreferenceManager().getCookie();
        mProgressDialog = new ProgressDialog(this);
        mGrpId = intent.getStringExtra("grp_id");
        mArtlNum = intent.getStringExtra("artl_num");
        mTitle = intent.getStringExtra("sbjt");
        mContent = intent.getStringExtra("txt");
        mImageList = intent.getStringArrayListExtra("img");
        mGrpKey = intent.getStringExtra("grp_key");
        mArtlKey = intent.getStringExtra("artl_key");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonImage.setOnClickListener(this::showContextMenu);
        buttonVideo.setOnClickListener(this::showContextMenu);

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("title", mTitle);
        headerMap.put("content", mContent);
        mAdapter.addHeaderView(headerMap);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mProgressDialog.setCancelable(false);
        if (mImageList.size() > 0) {
            mImageList.forEach(s -> mContents.add(new WriteItem(null, null, s)));
            mAdapter.notifyDataSetChanged();
        }
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
                String title = (String) mAdapter.getTextMap().get("title");
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml((Spanned) mAdapter.getTextMap().get("content"), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml((Spanned) mAdapter.getTextMap().get("content"));
                if (!title.isEmpty() && !(TextUtils.isEmpty(content) && mContents.size() < 2)) {
                    mMakeHtmlImages = new StringBuilder();
                    mImageList.clear();
                    mProgressDialog.setMessage("전송중...");
                    mProgressDialog.setProgressStyle(mContents.size() > 1 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();

                    if (mContents.size() > 1) {
                        int position = 1;
                        uploadImage(position, mContents.get(position));
                    } else
                        actionSend(title, content);
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
                menu.add(3, Menu.NONE, Menu.NONE, "유튜브");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getGroupId()) {
            case 0:
                int position = item.getItemId();
                mContents.remove(position);
                mAdapter.notifyItemRemoved(position);
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
                Intent ysIntent = new Intent(getApplicationContext(), YouTubeSearchActivity.class);
                startActivity(ysIntent);
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

    private void uploadImage(int position, WriteItem writeItem) {
        if (writeItem.getImage() != null)
            imageUploadProcess(position, writeItem.getImage(), false);
        else {
            MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, response -> {
                String imageSrc = new String(response.data);
                imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles/"), imageSrc.lastIndexOf("\""));
                imageUploadProcess(position, imageSrc, true);
            }, error -> {
                VolleyLog.e(TAG, error.getMessage());
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
                    params.put("file", new MultipartRequest.DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(writeItem.getBitmap())));
                    return params;
                }

                private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                }
            };
            AppController.getInstance().addToRequestQueue(multipartRequest);
        }
    }

    private void imageUploadProcess(int count, String imageUrl, boolean isFlag) {
        mImageList.add(imageUrl);
        mProgressDialog.setProgress((int) ((double) (count) / (mContents.size() - 1) * 100));
        try {
            mMakeHtmlImages.append("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>" + (count < mContents.size() - 1 ? "<br>": ""));
            if (count < mContents.size() - 1) {
                count++;
                Thread.sleep(isFlag ? 700 : 0);
                uploadImage(count, mContents.get(count));
            } else {
                String title = (String) mAdapter.getTextMap().get("title");
                String content = (!TextUtils.isEmpty(mAdapter.getTextMap().get("content").toString()) ? Html.toHtml((Spanned) mAdapter.getTextMap().get("content"), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlImages.toString();
                actionSend(title, content);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_LONG).show();
            hideProgressDialog();
        }
    }

    private void actionSend(String title, String content) {
        String tagStringReq = "req_send";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, response -> {
            try {
                hideProgressDialog();

                Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ModifyActivity.this, ArticleActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                initFirebaseData();
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
                params.put("CLUB_GRP_ID", mGrpId);
                params.put("ARTL_NUM", mArtlNum);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
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

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        updateArticleDataToFirebase(databaseReference.child(mGrpKey).child(mArtlKey));
    }

    private void updateArticleDataToFirebase(DatabaseReference query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);
                if (articleItem != null) {
                    articleItem.setTitle((String) mAdapter.getTextMap().get("title"));
                    articleItem.setContent(TextUtils.isEmpty(mAdapter.getTextMap().get("content").toString()) ? null : mAdapter.getTextMap().get("content").toString());
                    articleItem.setImages(mImageList.isEmpty() ? null : mImageList);
                    query.getRef().setValue(articleItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
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
