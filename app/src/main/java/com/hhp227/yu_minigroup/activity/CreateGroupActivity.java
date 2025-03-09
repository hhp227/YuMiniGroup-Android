package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.ActivityCreateGroupBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.handler.OnActivityCreateGroupEventListener;
import com.hhp227.yu_minigroup.helper.BitmapUtil;
import com.hhp227.yu_minigroup.viewmodel.CreateGroupViewModel;

public class CreateGroupActivity extends AppCompatActivity implements OnActivityCreateGroupEventListener {
    private ActivityCreateGroupBinding mBinding;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher;

    private CreateGroupViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);
        mViewModel = new ViewModelProvider(this).get(CreateGroupViewModel.class);
        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setHandler(this);
        setAppBar(mBinding.toolbar);
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
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
                String title = mBinding.etTitle.getText().toString().trim();
                String description = mBinding.etDescription.getText().toString().trim();

                mViewModel.createGroup(title, description);
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

                mCameraCaptureActivityResultLauncher.launch(cameraIntent);
                break;
            case "갤러리":
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);

                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mCameraPickActivityResultLauncher.launch(galleryIntent);
                break;
            case "이미지 없음":
                mViewModel.setBitmap(null);
                Snackbar.make(getCurrentFocus(), "이미지 없음 선택", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onImageClick(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
        unregisterForContextMenu(v);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModelData() {
        mViewModel.getGroupItemEntry().observe(this, groupItemEntry -> {
            if (groupItemEntry != null) {
                Intent intent = new Intent(CreateGroupActivity.this, GroupActivity.class);
                GroupItem groupItem = groupItemEntry.getValue();

                intent.putExtra("admin", true);
                intent.putExtra("grp_id", groupItem.getId());
                intent.putExtra("grp_nm", groupItem.getName());
                intent.putExtra("grp_img", groupItem.getImage());
                intent.putExtra("key", groupItemEntry.getKey());
                setResult(RESULT_OK, intent);
                startActivity(intent);
                finish();
            }
        });
        mViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(getCurrentFocus(), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        mViewModel.getTitleError().observe(this, message -> mBinding.etTitle.setError(message));
        mViewModel.getDescriptionError().observe(this, message -> mBinding.etDescription.setError(message));
        mViewModel.getBitmap().observe(this, bitmap -> {
            if (bitmap != null) {
                mBinding.ivGroupImage.setImageBitmap(bitmap);
            } else {
                mBinding.ivGroupImage.setImageResource(R.drawable.add_photo);
            }
        });
    }

    private void onCameraActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            if (result.getData().getExtras().get("data") != null) {
                mViewModel.setBitmap((Bitmap) result.getData().getExtras().get("data"));
            } else if (result.getData().getData() != null) {
                mViewModel.setBitmap(new BitmapUtil(this).bitmapResize(result.getData().getData(), 200));
            }
        }
    }
}
