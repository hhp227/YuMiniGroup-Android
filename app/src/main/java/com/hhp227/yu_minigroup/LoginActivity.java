package com.hhp227.yu_minigroup;

import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.b_login);

        login.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "준비중입니다.", Toast.LENGTH_LONG).show();
        });
    }
}
