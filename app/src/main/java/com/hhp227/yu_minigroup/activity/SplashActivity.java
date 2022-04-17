package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.ActivitySplashBinding;
import com.hhp227.yu_minigroup.viewmodel.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 1250;

    private ActivitySplashBinding mBinding;

    private SplashViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        Handler handler = new Handler(getMainLooper());
        mViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        Window window = getWindow();

        setContentView(mBinding.getRoot());
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        handler.postDelayed(() -> mViewModel.loginLMS(null, null), SPLASH_TIME_OUT);
        mViewModel.mState.observe(this, state -> {
            if (state.isSuccess) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
                overridePendingTransition(R.anim.splash_in, R.anim.splash_out);
            } else {
                if (state.message != null) {
                    Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}
