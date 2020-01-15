package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.fragment.GroupFragment;

public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Button button = findViewById(R.id.b_test);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GroupFragment.class);
            setResult(RESULT_OK, intent);
        });
    }
}
