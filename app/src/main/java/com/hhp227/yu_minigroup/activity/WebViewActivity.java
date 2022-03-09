package com.hhp227.yu_minigroup.activity;

import android.view.MenuItem;
import android.webkit.WebSettings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hhp227.yu_minigroup.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebViewBinding binding = ActivityWebViewBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        WebSettings webSettings = binding.wvNews.getSettings();
        String url = getIntent().getStringExtra("url");

        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("title"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding.wvNews.loadUrl(url);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
