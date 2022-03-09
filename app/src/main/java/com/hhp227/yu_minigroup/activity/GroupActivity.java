package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.fragment.TabHostLayoutFragment;

public class GroupActivity extends AppCompatActivity {
    private String mGroupName, mKey;

    private TabHostLayoutFragment mFragMain;

    private ActivityGroupBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGroupBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        Intent intent = getIntent();
        boolean isAdmin = intent.getBooleanExtra("admin", false);
        String groupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        String groupImage = intent.getStringExtra("grp_img");
        int position = intent.getIntExtra("pos", 0);
        mKey = intent.getStringExtra("key");
        mFragMain = TabHostLayoutFragment.newInstance(isAdmin, groupId, mGroupName, groupImage, position, mKey);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragMain).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.action_chat) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);

            intent.putExtra("grp_chat", true);
            intent.putExtra("chat_nm", mGroupName);
            intent.putExtra("uid", mKey);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragMain.onActivityResult(requestCode, resultCode, data);
    }
}
