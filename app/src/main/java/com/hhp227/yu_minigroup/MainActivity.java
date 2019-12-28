package com.hhp227.yu_minigroup;

import android.widget.TextView;
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
    private DrawerLayout drawerLayout;
    private PreferenceManager preferenceManager;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
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
            }
            if (fragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        //textView = findViewById(R.id.tv_test);
        preferenceManager = AppController.getInstance().getPreferenceManager();
        //textView.setText(getIntent().getStringExtra("response") + " | 테스트");
        /*textView.setOnClickListener(v -> {
            preferenceManager.clear();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        textView.setOnLongClickListener(v -> {
            android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(textView.getText());
            Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
            return true;
        });*/
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}
