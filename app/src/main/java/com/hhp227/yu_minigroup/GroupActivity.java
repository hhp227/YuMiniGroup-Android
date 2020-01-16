package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.fragment.GroupFragment;

public class GroupActivity extends AppCompatActivity {
    private boolean isAdmin;
    private int position;
    private String groupId, groupName, key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Button button = findViewById(R.id.b_test);
        Intent intent = getIntent();
        isAdmin = intent.getBooleanExtra("admin", false);
        groupId = intent.getStringExtra("grp_id");
        groupName = intent.getStringExtra("grp_nm");
        position = intent.getIntExtra("pos", 0);
        button.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Is admin : " + isAdmin + ", Position : " + position + ", groupId : " + groupId + ", groupName : " + groupName, Toast.LENGTH_LONG).show();
        });
    }
}
