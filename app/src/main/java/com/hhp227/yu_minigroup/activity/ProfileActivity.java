package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityProfileBinding;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.viewmodel.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding mBinding;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher;

    private ProfileViewModel mViewModel;

    private Snackbar mProgressSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.ivProfileImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            openContextMenu(v);
            unregisterForContextMenu(v);
        });
        mBinding.bSync.setOnClickListener(v -> mViewModel.sync());
        mViewModel.getBitmap().observe(this, bitmap -> {
            Glide.with(getApplicationContext())
                    .load(bitmap)
                    .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                    .into(mBinding.ivProfileImage);
            invalidateOptionsMenu();
        });
        mViewModel.getState().observe(this, state -> {
            if (state.isLoading) {
                showProgressBar();
            } else if (state.user != null) {
                mBinding.tvName.setText(state.user.getName());
                mBinding.tvYuId.setText(state.user.getUserId());
                mBinding.tvDept.setText(state.user.getDepartment());
                mBinding.tvGrade.setText(state.user.getGrade());
                mBinding.tvEmail.setText(state.user.getEmail());
                mBinding.tvPhoneNum.setText(state.user.getPhoneNumber());
                Glide.with(getApplicationContext())
                        .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", state.user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mViewModel.getCookie()).build()))
                        .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle)
                                .circleCrop()
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(mBinding.ivProfileImage);
                if (state.isSuccess) {
                    setResult(RESULT_OK);
                    Snackbar.make(findViewById(android.R.id.content), state.message, Snackbar.LENGTH_LONG).show();
                }
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(findViewById(android.R.id.content), state.message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
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
                mCameraPickActivityResultLauncher.launch(intent);
                return true;
            case 1:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                mCameraCaptureActivityResultLauncher.launch(intent);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_send);

        menuItem.setVisible(mViewModel.getBitmap().getValue() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                mViewModel.uploadImage(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCameraActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            if (result.getData().getExtras().get("data") != null) {
                mViewModel.setBitmap((Bitmap) result.getData().getExtras().get("data"));
            } else if (result.getData().getData() != null) {
                mViewModel.setBitmap(new BitmapUtil(getBaseContext()).bitmapResize(result.getData().getData(), 200));
            }
        }
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
