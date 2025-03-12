package com.hhp227.yu_minigroup.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.ActivityUpdateReplyBinding;
import com.hhp227.yu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.yu_minigroup.viewmodel.UpdateReplyViewModel;

public class UpdateReplyActivity extends AppCompatActivity {
    private Snackbar mProgressSnackBar;

    private ActivityUpdateReplyBinding mBinding;

    private UpdateReplyViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_reply);
        mViewModel = new ViewModelProvider(this).get(UpdateReplyViewModel.class);

        setAppBar(mBinding.toolbar);
        mBinding.rvWrite.setAdapter(new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ModifyTextBinding binding = ModifyTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                binding.setLifecycleOwner(UpdateReplyActivity.this);
                binding.setViewModel(mViewModel);
                return new Holder(binding);
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int position) {
                holder.bind();
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send) {
            String text = mViewModel.text.getValue();

            mViewModel.actionSend(text);
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
        mViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                showProgressBar();
            } else {
                hideProgressBar();
            }
        });
        mViewModel.getReply().observe(this, reply -> {
            if (!TextUtils.isEmpty(reply)) {
                Intent intent = new Intent(UpdateReplyActivity.this, ArticleActivity.class);

                // 입력 자판 숨기기
                View view = UpdateReplyActivity.this.getCurrentFocus();

                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                intent.putExtra("update_reply", reply);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mViewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(getCurrentFocus(), message, Snackbar.LENGTH_LONG).show();
            }
        });
        mViewModel.getReplyError().observe(this, replyError -> Snackbar.make(getCurrentFocus(), replyError, Snackbar.LENGTH_LONG).show());
    }

    private void setProgressBar() {
        mProgressSnackBar = Snackbar.make(getCurrentFocus(), "전송중...", Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) mProgressSnackBar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();

        contentLay.addView(new ProgressBar(getApplicationContext()));
    }

    private void showProgressBar() {
        setProgressBar();
        if (!mProgressSnackBar.isShown())
            mProgressSnackBar.show();
    }

    private void hideProgressBar() {
        if (mProgressSnackBar.isShown())
            mProgressSnackBar.dismiss();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final ModifyTextBinding mBinding;

        Holder(ModifyTextBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind() {
            mBinding.executePendingBindings();
        }
    }
}