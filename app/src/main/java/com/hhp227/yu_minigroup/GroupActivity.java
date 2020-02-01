package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hhp227.yu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.yu_minigroup.fragment.GroupFragment;
import com.hhp227.yu_minigroup.fragment.TabHostLayoutFragment;

public class GroupActivity extends AppCompatActivity {
    private boolean mIsAdmin;
    private int mPosition;
    private String mGroupId, mGroupName, mGroupImage, mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Intent intent = getIntent();
        mIsAdmin = intent.getBooleanExtra("admin", false);
        mGroupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        mGroupImage = intent.getStringExtra("grp_img");
        mPosition = intent.getIntExtra("pos", 0);
        mKey = intent.getStringExtra("key");
        TabHostLayoutFragment fragMain = TabHostLayoutFragment.newInstance(mIsAdmin, mGroupId, mGroupName, mGroupImage, mPosition, mKey);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMain).commit();
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
}
