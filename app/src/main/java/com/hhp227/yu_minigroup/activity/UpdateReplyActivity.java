package com.hhp227.yu_minigroup.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.databinding.ActivityUpdateReplyBinding;
import com.hhp227.yu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.yu_minigroup.viewmodel.UpdateReplyViewModel;

// TODO
public class UpdateReplyActivity extends AppCompatActivity {
    private Holder mHolder;

    private Snackbar mProgressSnackBar;

    private ActivityUpdateReplyBinding mBinding;

    private UpdateReplyViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityUpdateReplyBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(UpdateReplyViewModel.class);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.rvWrite.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvWrite.setAdapter(new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mHolder = new Holder(ModifyTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                return mHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int position) {
                holder.bind(mViewModel.getReply());
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
        mViewModel.getState().observe(this, state -> {
            if (state.isLoading) {
                showProgressBar();
            } else if (state.text != null && !state.text.isEmpty()) {
                Intent intent = new Intent(UpdateReplyActivity.this, ArticleActivity.class);

                // 입력 자판 숨기기
                View view = UpdateReplyActivity.this.getCurrentFocus();

                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                hideProgressBar();
                intent.putExtra("update_reply", state.text);
                setResult(RESULT_OK, intent);
                finish();
            } else if (state.message != null && !state.message.isEmpty()) {
                hideProgressBar();
                Snackbar.make(getCurrentFocus(), state.message, Snackbar.LENGTH_LONG).show();
            }
        });
        mViewModel.getReplyFormState().observe(this, replyFormState -> Snackbar.make(getCurrentFocus(), replyFormState.replyError, Snackbar.LENGTH_LONG).show());
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
            String text = mHolder.mBinding.etReply.getText().toString().trim();

            mViewModel.actionSend(text);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        public void bind(String reply) {
            mBinding.etReply.setText(reply);
        }
    }
}
