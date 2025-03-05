package com.hhp227.yu_minigroup.activity;

import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.yu_minigroup.adapter.PicturePagerAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityPictureBinding;

import java.util.List;

// TODO
public class PictureActivity extends AppCompatActivity {
    private List<String> mImages;

    private ActivityPictureBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPictureBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        int position = 0;
        Bundle b = getIntent().getExtras();

        if (b != null) {
            mImages = b.getStringArrayList("images");
            position = b.getInt("position");
        }
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.viewPager.setAdapter(new PicturePagerAdapter(mImages));
        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBinding.tvCount.setText((position + 1) + " / " + mImages.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mBinding.viewPager.setCurrentItem(position, false);
        mBinding.tvCount.setVisibility(mImages.size() > 1 ? View.VISIBLE : View.GONE);
        mBinding.tvCount.setText((position + 1) + " / " + mImages.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding = null;
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
