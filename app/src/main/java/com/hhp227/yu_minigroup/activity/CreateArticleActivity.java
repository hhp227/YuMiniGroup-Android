package com.hhp227.yu_minigroup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.WriteListAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityCreateArticleBinding;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.viewmodel.CreateArticleViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateArticleActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;

    private ProgressDialog mProgressDialog;

    private Uri mPhotoUri;

    private WriteListAdapter mAdapter;

    private ActivityCreateArticleBinding mBinding;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher, mYouTubeSearchActivityResultLauncher;

    private CreateArticleViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCreateArticleBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(CreateArticleViewModel.class);
        mAdapter = new WriteListAdapter(mViewModel.mContents);
        mProgressDialog = new ProgressDialog(this);

        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Uri fileUri = result.getData().getData();
                    Bitmap bitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);

                    mViewModel.setBitmap(bitmap);
                });
            }
        });
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Bitmap bitmap = new BitmapUtil(this).bitmapResize(mPhotoUri, 200);

                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                            int angle = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90
                                    : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                                    : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270
                                    : 0;
                            Bitmap rotatedBitmap = new BitmapUtil(this).rotateImage(bitmap, angle);

                            mViewModel.setBitmap(rotatedBitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        mYouTubeSearchActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                YouTubeItem youTubeItem = result.getData().getParcelableExtra("youtube");

                mViewModel.setYoutube(youTubeItem);
            }
        });
        Map<String, Object> headerMap = new HashMap<>();

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        headerMap.put("title", getIntent().getStringExtra("sbjt"));
        headerMap.put("content", getIntent().getStringExtra("txt"));
        mAdapter.addHeaderView(headerMap);
        mBinding.rvWrite.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvWrite.setAdapter(mAdapter);
        mBinding.llImage.setOnClickListener(this::showContextMenu);
        mBinding.llVideo.setOnClickListener(this::showContextMenu);
        mProgressDialog.setMessage("전송중...");
        mProgressDialog.setCancelable(false);
        mViewModel.getYoutubeState().observe(this, youTubeItem -> {
            if (youTubeItem != null) {
                if (youTubeItem.position > -1) {
                    mViewModel.addItem(youTubeItem.position + 1, youTubeItem);
                } else {
                    mViewModel.addItem(youTubeItem);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mViewModel.getState().observe(this, state -> {
            if (state.progress >= 0) {
                mProgressDialog.setProgressStyle(mViewModel.mContents.size() > 1 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setProgress(state.progress);
                showProgressDialog();
            } else if (state.articleId != null && !state.articleId.isEmpty()) {
                setResult(RESULT_OK);
                finish();
                Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                hideProgressDialog();
            } else if (state.message != null && !state.message.isEmpty()) {
                Snackbar.make(mBinding.rvWrite, state.message, Snackbar.LENGTH_LONG).show();
                hideProgressDialog();
            }
        });
        mViewModel.getBitmapState().observe(this, bitmap -> {
            mViewModel.addItem(bitmap);
            mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
        });
        mViewModel.getArticleFormState().observe(this, articleFormState -> Snackbar.make(getCurrentFocus(), articleFormState.message, Snackbar.LENGTH_LONG).setAction("Action", null).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
        mYouTubeSearchActivityResultLauncher = null;
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
                Spannable title = (Editable) mAdapter.getTextMap().get("title");
                Spannable content = (Editable) mAdapter.getTextMap().get("content");

                mViewModel.actionSend(title, content);
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
                if (mViewModel.mContents.get(item.getItemId()) instanceof YouTubeItem) {
                    mViewModel.setYoutube(null);
                }
                mViewModel.removeItem(item.getItemId());
                mAdapter.notifyItemRemoved(item.getItemId());
                return true;
            case 1:
                intent = new Intent(Intent.ACTION_PICK);

                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mCameraPickActivityResultLauncher.launch(intent);
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
                        mCameraCaptureActivityResultLauncher.launch(intent);
                    }
                }
                return true;
            case 3:
                if (mViewModel.getYoutubeState().getValue() != null)
                    Snackbar.make(getCurrentFocus(), "동영상은 하나만 첨부 할수 있습니다.", Snackbar.LENGTH_LONG).show();
                else {
                    Intent ysIntent = new Intent(getApplicationContext(), YouTubeSearchActivity.class);

                    mYouTubeSearchActivityResultLauncher.launch(ysIntent);
                }
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

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
