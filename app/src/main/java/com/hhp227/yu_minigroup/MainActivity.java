package com.hhp227.yu_minigroup;

import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv_test);

        textView.setOnClickListener(v -> {
                Toast.makeText(getApplicationContext(), "테스트", Toast.LENGTH_LONG).show();
            }
        );
    }
}
