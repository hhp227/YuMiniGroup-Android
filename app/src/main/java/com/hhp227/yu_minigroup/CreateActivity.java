package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class CreateActivity extends AppCompatActivity {
    // 인텐트값
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 200;
    private static final String TAG = CreateActivity.class.getSimpleName();
    private Bitmap bitmap;
    private EditText groupTitle, groupDescription;
    private ImageView groupImage, resetTitle;
    private PreferenceManager preferenceManager;
    private RadioGroup joinType;

    private boolean joinTypeCheck;
    private String cookie, pushId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Toolbar toolbar = findViewById(R.id.toolbar);
        groupTitle = findViewById(R.id.et_title);
        groupDescription = findViewById(R.id.et_description);
        resetTitle = findViewById(R.id.iv_reset);
        groupImage = findViewById(R.id.iv_group_image);
        joinType = findViewById(R.id.rg_jointype);
        preferenceManager = AppController.getInstance().getPreferenceManager();
        cookie = preferenceManager.getCookie();

        setSupportActionBar(toolbar);
        groupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetTitle.setImageResource(s.length() > 0 ? R.drawable.ic_clear_black_24dp : R.drawable.ic_clear_gray_24dp);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        resetTitle.setOnClickListener(v -> {
            groupTitle.setText("");
        });
        groupImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            openContextMenu(v);
            unregisterForContextMenu(v);
        });
        joinType.setOnCheckedChangeListener((group, checkedId) -> {
            joinTypeCheck = checkedId != R.id.rb_auto;
        });
        joinType.check(R.id.rb_auto);
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
                String title = groupTitle.getText().toString().trim();
                String description = groupDescription.getText().toString().trim();
                String join = !joinTypeCheck ? "0" : "1";
                if (!title.isEmpty() && !description.isEmpty()) {

                } else {
                    groupTitle.setError(title.isEmpty() ? "그룹명을 입력하세요." : null);
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
                groupImage.setImageResource(R.drawable.add_photo);
                bitmap = null;
                Toast.makeText(getBaseContext(), "이미지 없음 선택", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }
}
