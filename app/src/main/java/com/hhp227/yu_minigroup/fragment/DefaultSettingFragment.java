package com.hhp227.yu_minigroup.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.FragmentDefaultSettingBinding;
import com.hhp227.yu_minigroup.handler.OnFragmentDefaultSettingEventListener;
import com.hhp227.yu_minigroup.helper.BitmapUtil;

import com.hhp227.yu_minigroup.viewmodel.DefaultSettingViewModel;

public class DefaultSettingFragment extends Fragment implements OnFragmentDefaultSettingEventListener {
    private static final String GROUP_ID = "grp_id";

    private static final String GROUP_IMAGE = "grp_img";

    private static final String GROUP_KEY = "key";

    private FragmentDefaultSettingBinding mBinding;

    private DefaultSettingViewModel mViewModel;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher;

    public DefaultSettingFragment() {
    }

    public static DefaultSettingFragment newInstance(String grpId, String grpImg, String key) {
        DefaultSettingFragment fragment = new DefaultSettingFragment();
        Bundle args = new Bundle();

        args.putString(GROUP_ID, grpId);
        args.putString(GROUP_IMAGE, grpImg);
        args.putString(GROUP_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentDefaultSettingBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(DefaultSettingViewModel.class);
        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCameraActivityResult);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modify, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            final String groupName = mBinding.etTitle.getText().toString();
            final String groupDescription = mBinding.etDescription.getText().toString();

            mViewModel.updateGroup(groupName, groupDescription);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("이미지 선택");
        menu.add("카메라");
        menu.add("갤러리");
        menu.add("이미지 없음");
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

                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.CONTENT_TYPE);
                mCameraPickActivityResultLauncher.launch(galleryIntent);
                break;
            case "이미지 없음":
                mViewModel.setBitmap(null);
                mBinding.ivGroupImage.setImageResource(R.drawable.add_photo);
                Toast.makeText(getContext(), "이미지 없음 선택", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onImageClick(View v) {
        registerForContextMenu(v);
        requireActivity().openContextMenu(v);
        unregisterForContextMenu(v);
    }

    private void observeViewModelData() {
        mViewModel.getUpdatedGroupItem().observe(getViewLifecycleOwner(), groupItem -> {
            if (groupItem != null) {
                Intent intent = new Intent(getContext(), Tab4Fragment.class);

                intent.putExtra("grp_nm", groupItem.getName());
                intent.putExtra("grp_desc", groupItem.getDescription());
                intent.putExtra("join_div", groupItem.getJoinType());
                requireActivity().setResult(RESULT_OK, intent);
                requireActivity().finish();
                Toast.makeText(getContext(), "소모임 변경 완료", Toast.LENGTH_LONG).show();
            }
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
        mViewModel.getTitleError().observe(getViewLifecycleOwner(), message -> mBinding.etTitle.setError(message));
        mViewModel.getDescriptionError().observe(getViewLifecycleOwner(), message -> mBinding.etDescription.setError(message));
        mViewModel.getBitmap().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null) {
                mBinding.ivGroupImage.setImageBitmap(bitmap);
            }
        });
    }

    private void onCameraActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            if (result.getData().getExtras().get("data") != null) {
                mViewModel.setBitmap((Bitmap) result.getData().getExtras().get("data"));
            } else if (result.getData().getData() != null) {
                mViewModel.setBitmap(new BitmapUtil(requireContext()).bitmapResize(result.getData().getData(), 200));
            }
        }
    }
}
