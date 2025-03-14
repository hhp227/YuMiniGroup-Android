package com.hhp227.yu_minigroup.activity;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.PicturePagerAdapter;
import com.hhp227.yu_minigroup.databinding.ActivityPictureBinding;
import com.hhp227.yu_minigroup.viewmodel.PictureViewModel;

import java.util.Objects;

public class PictureActivity extends AppCompatActivity {
    private PictureViewModel mViewModel;

    private ActivityPictureBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_picture);
        mViewModel = new ViewModelProvider(this).get(PictureViewModel.class);

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        setAppBar(mBinding.toolbar);
        mBinding.viewPager.setAdapter(new PicturePagerAdapter());
        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mViewModel.setPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        observeViewModelData();
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

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModelData() {
        mViewModel.getImageList().observe(this, ((PicturePagerAdapter) Objects.requireNonNull(mBinding.viewPager.getAdapter()))::submitList);
        mViewModel.getPosition().observe(this, mBinding.viewPager::setCurrentItem);
    }
}
