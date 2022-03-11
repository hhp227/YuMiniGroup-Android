package com.hhp227.yu_minigroup.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityUpdateCommentBinding;
import com.hhp227.yu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.yu_minigroup.dto.ReplyItem;

import java.util.HashMap;
import java.util.Map;

public class UpdateCommentActivity extends AppCompatActivity {
    private static final String TAG = "댓글수정";

    private Holder mHolder;

    private String mGroupId, mArticleId, mReplyId, mReply, mArticleKey, mReplyKey;

    private Snackbar mProgressSnackBar;

    private ActivityUpdateCommentBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityUpdateCommentBinding.inflate(getLayoutInflater());
        mGroupId = getIntent().getStringExtra("grp_id");
        mArticleId = getIntent().getStringExtra("artl_num");
        mReplyId = getIntent().getStringExtra("cmmt_num");
        mArticleKey = getIntent().getStringExtra("artl_key");
        mReplyKey = getIntent().getStringExtra("cmmt_key");
        mReply = getIntent().getStringExtra("cmt");
        mReply = mReply.contains("※") ? mReply.substring(0, mReply.lastIndexOf("※")).trim() : mReply;

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
                holder.bind(mReply);
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.hhp227.yu_minigroup.R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send) {
            String text = mHolder.mBinding.etReply.getText().toString().trim();

            if (!TextUtils.isEmpty(text)) {
                String tag_string_req = "req_send";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_REPLY, response -> {
                    try {

                        // 입력 자판 숨기기
                        View view = UpdateCommentActivity.this.getCurrentFocus();
                        Intent intent = new Intent(UpdateCommentActivity.this, ArticleActivity.class);

                        if (view != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        intent.putExtra("update_reply", response);
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        initFirebaseData();
                    }
                }, error -> {
                    VolleyLog.e(TAG, error.getMessage());
                    hideProgressBar();
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();

                        headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        params.put("CLUB_GRP_ID", mGroupId);
                        params.put("ARTL_NUM", mArticleId);
                        params.put("CMMT_NUM", mReplyId);
                        params.put("CMT", text);
                        return params;
                    }
                };
                showProgressBar();
                AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
            } else
                Snackbar.make(getCurrentFocus(), "내용을 입력하세요.", Snackbar.LENGTH_LONG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        updateReplyDataToFirebase(databaseReference.child(mArticleKey).child(mReplyKey));
    }

    private void updateReplyDataToFirebase(final Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);

                    replyItem.setReply(mHolder.mBinding.etReply.getText().toString() + "\n");
                    query.getRef().setValue(replyItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
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
