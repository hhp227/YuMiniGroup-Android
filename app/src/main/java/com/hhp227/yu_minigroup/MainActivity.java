package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.fragment.*;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private PreferenceManager mPreferenceManager;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView name = navigationView.getHeaderView(0).findViewById(R.id.tv_name);
        mProfileImage = navigationView.getHeaderView(0).findViewById(R.id.iv_profile_image);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new GroupFragment()).commit();
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.nav_menu1:
                    fragment = new GroupFragment();
                    break;
                case R.id.nav_menu2:
                    fragment = new UnivNoticeFragment();
                    break;
                case R.id.nav_menu3:
                    fragment = new TimetableFragment();
                    break;
                case R.id.nav_menu4:
                    fragment = new SeatFragment();
                    break;
                case R.id.nav_menu5:
                    fragment = new BusFragment();
                    break;
                case R.id.nav_menu6:
                    mPreferenceManager.clear();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
            }
            if (fragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.commit();
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        Glide.with(this)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie())
                        .build()))
                .apply(new RequestOptions().centerCrop()
                        .error(R.drawable.user_image_view)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mProfileImage);
        mProfileImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));
        name.setText(mPreferenceManager.getUser().getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder().addHeader("Cookie", mPreferenceManager.getCookie()).build()))
                .apply(new RequestOptions().circleCrop()
                        .error(R.drawable.user_image_view_circle)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mProfileImage);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}
