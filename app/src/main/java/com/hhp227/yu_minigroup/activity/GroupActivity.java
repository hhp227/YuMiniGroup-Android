package com.hhp227.yu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.ActivityGroupBinding;
import com.hhp227.yu_minigroup.fragment.TabHostLayoutFragment;
import com.hhp227.yu_minigroup.viewmodel.GroupViewModel;

public class GroupActivity extends AppCompatActivity {
    private TabHostLayoutFragment mFragMain;

    private ActivityGroupBinding mBinding;

    private GroupViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGroupBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        mFragMain = TabHostLayoutFragment.newInstance(mViewModel.isAdmin, mViewModel.mGroupId, mViewModel.mGroupName, mViewModel.mGroupImage, mViewModel.mPosition, mViewModel.mKey);

        setContentView(mBinding.getRoot());
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
            intent.putExtra("chat_nm", mViewModel.mGroupName);
            intent.putExtra("uid", mViewModel.mKey);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onProfileActivityResult(ActivityResult result) {
        mFragMain.onProfileActivityResult(result);
        setResult(RESULT_OK);
    }
}
