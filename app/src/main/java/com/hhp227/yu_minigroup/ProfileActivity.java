package com.hhp227.yu_minigroup;

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ImageView profileImage = findViewById(R.id.iv_profile_image);
        TextView name = findViewById(R.id.tv_name);
        TextView knuId = findViewById(R.id.tv_yu_id);
        TextView department = findViewById(R.id.tv_dept);
        TextView number = findViewById(R.id.tv_stu_num);
        TextView grade = findViewById(R.id.tv_grade);
        TextView email = findViewById(R.id.tv_email);
        PreferenceManager preferenceManager = AppController.getInstance().getPreferenceManager();
        User user = preferenceManager.getUser();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", preferenceManager.getCookie()).build()))
                .into(profileImage);
        name.setText(user.getName());
        knuId.setText(user.getUserId());
        department.setText(user.getDepartment());
        number.setText(user.getNumber());
        grade.setText(user.getGrade());
        email.setText(user.getEmail());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
