package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.fragment.GroupFragment;

public class GroupActivity extends AppCompatActivity {
    private boolean mIsAdmin;
    private int mPosition;
    private String mGroupId, mGroupName, mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Button button = findViewById(R.id.b_test);
        Intent intent = getIntent();
        mIsAdmin = intent.getBooleanExtra("admin", false);
        mGroupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        mPosition = intent.getIntExtra("pos", 0);
        button.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Is admin : " + mIsAdmin + ", Position : " + mPosition + ", groupId : " + mGroupId + ", groupName : " + mGroupName, Toast.LENGTH_LONG).show();
        });
    }
}
