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
    private TextView mCount;
    private List<String> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ViewPager viewPager = findViewById(R.id.view_pager);
        mCount = findViewById(R.id.tv_count);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int position = 0;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mImages = b.getStringArrayList("images");
            position = b.getInt("position");
        }
        PicturePagerAdapter pagerAdapter = new PicturePagerAdapter(this, mImages);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        viewPager.setCurrentItem(position, false);
        mCount.setVisibility(mImages.size() > 1 ? View.VISIBLE : View.GONE);
        mCount.setText((position + 1) + " / " + mImages.size());
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
