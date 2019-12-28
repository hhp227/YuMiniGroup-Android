package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.dto.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }, SPLASH_TIME_OUT);
    }
}
