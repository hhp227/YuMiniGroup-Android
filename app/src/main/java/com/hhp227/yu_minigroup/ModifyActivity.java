package com.hhp227.yu_minigroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.yu_minigroup.adapter.WriteListAdapter;
import com.hhp227.yu_minigroup.dto.WriteItem;

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
                int position = item.getItemId();
                mImageList.remove(position - 1);
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
                Toast.makeText(getApplicationContext(), "동영상 선택", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
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
}
