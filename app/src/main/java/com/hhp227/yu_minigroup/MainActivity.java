package com.hhp227.yu_minigroup;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.fragment.GroupFragment;
import com.hhp227.yu_minigroup.fragment.UnivNoticeFragment;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private PreferenceManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
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
                    break;
                case R.id.nav_menu4:
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
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}
