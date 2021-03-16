package com.hhp227.yu_minigroup;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.yu_minigroup.adapter.PicturePagerAdapter;

import java.util.List;

public class PictureActivity extends AppCompatActivity {
    private List<String> mImages;

    private TextView mCount;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mCount = findViewById(R.id.tv_count);
        int position = 0;
        Bundle b = getIntent().getExtras();

        if (b != null) {
            mImages = b.getStringArrayList("images");
            position = b.getInt("position");
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager.setAdapter(new PicturePagerAdapter(mImages));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCount.setText((position + 1) + " / " + mImages.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(position, false);
        mCount.setVisibility(mImages.size() > 1 ? View.VISIBLE : View.GONE);
        mCount.setText((position + 1) + " / " + mImages.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
