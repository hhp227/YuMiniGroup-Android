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
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.hhp227.yu_minigroup.WriteActivity.*;

public class ModifyActivity extends AppCompatActivity {
    private static final String TAG = ModifyActivity.class.getSimpleName();
    private String mGrpId, mArtlNum, mCurrentPhotoPath, mCookie, mTitle, mContent, mGrpKey, mArtlKey;
    private List<String> mImageList;
    private List<Object> mContents;
    private ProgressDialog mProgressDialog;
    private StringBuilder mMakeHtmlContents;
    private Uri mPhotoUri;
    private WriteListAdapter mAdapter;
    private YouTubeItem mYouTubeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout buttonImage = findViewById(R.id.ll_image);
        LinearLayout buttonVideo = findViewById(R.id.ll_video);
        RecyclerView recyclerView = findViewById(R.id.rv_write);
        Intent intent = getIntent();
        Map<String, Object> headerMap = new HashMap<>();
        mContents = new ArrayList<>();
        mAdapter = new WriteListAdapter(this, mContents);
        mCookie = AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS);
        mProgressDialog = new ProgressDialog(this);
        mGrpId = intent.getStringExtra("grp_id");
        mArtlNum = intent.getStringExtra("artl_num");
        mTitle = intent.getStringExtra("sbjt");
        mContent = intent.getStringExtra("txt");
        mImageList = intent.getStringArrayListExtra("img");
        mYouTubeItem = intent.getParcelableExtra("vid");
        mGrpKey = intent.getStringExtra("grp_key");
        mArtlKey = intent.getStringExtra("artl_key");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonImage.setOnClickListener(this::showContextMenu);
        buttonVideo.setOnClickListener(this::showContextMenu);

        headerMap.put("title", mTitle);
        headerMap.put("content", mContent);
        mAdapter.addHeaderView(headerMap);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mProgressDialog.setCancelable(false);
        if (mImageList.size() > 0) {
            mContents.addAll(mImageList);
            mAdapter.notifyDataSetChanged();
        }
        if (mYouTubeItem != null)
            mContents.add(mYouTubeItem.position + 1, mYouTubeItem);
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
                    mMakeHtmlContents = new StringBuilder();

                    mImageList.clear();
                    mProgressDialog.setMessage("전송중...");
                    mProgressDialog.setProgressStyle(mContents.size() > 1 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();
                    if (mContents.size() > 1) {
                        int position = 1;
                        if (mContents.get(position) instanceof String) {
                            String image = (String) mContents.get(position);

                            uploadProcess(position, image, false);
                        } else if (mContents.get(position) instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) mContents.get(position);

                            uploadImage(position, bitmap);//
                        } else if (mContents.get(position) instanceof YouTubeItem) {
                            YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

                            uploadProcess(position, youTubeItem.videoId, true);
                        }
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
                if (mContents.get(position) instanceof YouTubeItem)
                    mYouTubeItem = null;

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
                if (mYouTubeItem != null)
                    Snackbar.make(getCurrentFocus(), "동영상은 하나만 첨부 할수 있습니다.", Snackbar.LENGTH_LONG).show();
                else {
                    Intent ysIntent = new Intent(getApplicationContext(), YouTubeSearchActivity.class);

                    ysIntent.putExtra("type", 1);
                    startActivityForResult(ysIntent, REQUEST_YOUTUBE_PICK);
                }
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

            mContents.add(bitmap);
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

                    mContents.add(rotatedBitmap);
                    mAdapter.notifyItemInserted(mContents.size() - 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_YOUTUBE_PICK && resultCode == RESULT_OK) {//
            mYouTubeItem = data.getParcelableExtra("youtube");

            mContents.add(mYouTubeItem);
            mAdapter.notifyItemInserted(mContents.size() - 1);
        }
    }

    private void uploadImage(int position, Bitmap bitmap) { // 수정
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, response -> {
            String imageSrc = new String(response.data);
            imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles/"), imageSrc.lastIndexOf("\""));

            uploadProcess(position, imageSrc, false);
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

                params.put("file", new MultipartRequest.DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(bitmap))); // 수정
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

    private void uploadProcess(int position, String imageUrl, boolean isYoutube) { // 수정
        if (!isYoutube)
            mImageList.add(imageUrl);
        mProgressDialog.setProgress((int) ((double) (position) / (mContents.size() - 1) * 100));
        try {
            String test = (isYoutube ? "<p><embed title=\"YouTube video player\" class=\"youtube-player\" autostart=\"true\" src=\"//www.youtube.com/embed/" + imageUrl + "?autoplay=1\"  width=\"488\" height=\"274\"></embed><p>" // 유튜브 태그
                    : ("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>")) + (position < mContents.size() - 1 ? "<br>": "");

            mMakeHtmlContents.append(test);
            if (position < mContents.size() - 1) {
                position++;
                Thread.sleep(!isYoutube ? 700 : 0);

                // 분기
                if (mContents.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
                    Bitmap bitmap = (Bitmap) mContents.get(position);// 수정

                    uploadImage(position, bitmap); // 수정
                } else if (mContents.get(position) instanceof String) {
                    String imageSrc = (String) mContents.get(position);

                    uploadProcess(position, imageSrc, false);
                } else if (mContents.get(position) instanceof YouTubeItem) {
                    YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

                    uploadProcess(position, youTubeItem.videoId, true);
                }
            } else {
                String title = (String) mAdapter.getTextMap().get("title");
                String content = (!TextUtils.isEmpty(mAdapter.getTextMap().get("content").toString()) ? Html.toHtml((Spanned) mAdapter.getTextMap().get("content"), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlContents.toString();

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
            hideProgressDialog();
            try {
                Intent intent = new Intent(ModifyActivity.this, ArticleActivity.class);

                Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
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
                    articleItem.setYoutube(mYouTubeItem);
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
