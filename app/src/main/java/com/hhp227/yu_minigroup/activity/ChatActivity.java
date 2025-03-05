package com.hhp227.yu_minigroup.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.adapter.MessageListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ActivityChatBinding;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.viewmodel.ChatViewModel;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO
public class ChatActivity extends AppCompatActivity {
    private static final int LIMIT = 15;

    private boolean mHasSelection, mIsGroupChat;

    private DatabaseReference mDatabaseReference;

    private MessageListAdapter mAdapter;

    private String mReceiver, mValue;

    private TextWatcher mTextWatcher;

    private View.OnLayoutChangeListener mOnLayoutChangeListener;

    private ActivityChatBinding mBinding;

    private ChatViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        mReceiver = getIntent().getStringExtra("uid");
        mValue = getIntent().getStringExtra("value");
        mIsGroupChat = getIntent().getBooleanExtra("grp_chat", false);
        mAdapter = new MessageListAdapter(mViewModel.mMessageItemList, mViewModel.getUser().getUid());
        mOnLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && mHasSelection)
                mBinding.rvMessage.post(() -> mBinding.rvMessage.scrollToPosition(mViewModel.mMessageItemList.size() - 1));
        };
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.cvBtnSend.setCardBackgroundColor(getResources().getColor(s.length() > 0 ? R.color.colorAccent : androidx.cardview.R.color.cardview_light_background, null));
                mBinding.tvBtnSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray, null));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("chat_nm") + (mIsGroupChat ? " 그룹채팅방" : ""));
        }
        mBinding.cvBtnSend.setOnClickListener(v -> {
            String message = mBinding.etInputMsg.getText().toString().trim();

            mViewModel.actionSend(message);
            mBinding.etInputMsg.setText("");
        });
        mBinding.etInputMsg.addTextChangedListener(mTextWatcher);
        mAdapter.setHasStableIds(true);
        layoutManager.setStackFromEnd(true);
        mBinding.rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mBinding.rvMessage.canScrollVertically(-1) && !mViewModel.mHasRequestedMore && mViewModel.mCursor != null) {
                    Log.e("TEST", "onScrollStateChanged");
                    mViewModel.mHasRequestedMore = true;

                    fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().endAt(mViewModel.mCursor).limitToLast(LIMIT) : mDatabaseReference.child(mViewModel.getUser().getUid()).child(mReceiver).orderByKey().endAt(mViewModel.mCursor).limitToLast(LIMIT), mViewModel.mMessageItemList.size(), mViewModel.mCursor);
                    mViewModel.mCursor = null;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mHasSelection = layoutManager.findFirstCompletelyVisibleItemPosition() + layoutManager.getChildCount() > layoutManager.getItemCount() - 2;
            }
        });
        mBinding.rvMessage.addOnLayoutChangeListener(mOnLayoutChangeListener);
        mBinding.rvMessage.setLayoutManager(layoutManager);
        mBinding.rvMessage.setAdapter(mAdapter);
        fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().limitToLast(LIMIT) : mDatabaseReference.child(mViewModel.getUser().getUid()).child(mReceiver).orderByKey().limitToLast(LIMIT), 0, "");
        mViewModel.getMessageFormState().observe(this, inputMessageFormState -> Toast.makeText(getBaseContext(), inputMessageFormState.messageError, Toast.LENGTH_LONG).show());
        /*mViewModel.mState.observe(this, state -> {
            if (state.isLoading) {
                Log.e("TEST", "isLoading");
            } else if (state.hasRequestedMore) {
                mAdapter.notifyDataSetChanged();
                Log.e("TEST", "prevCnt: " + state.offset);
                if (mHasSelection || mViewModel.mHasRequestedMore)
                    if (Integer.parseInt(state.offset) == 0) // 임시로 prevCnt를 state.offset에다 대입
                        mBinding.rvMessage.scrollToPosition(mViewModel.mMessageItemList.size() - 1);
                    else
                        ((LinearLayoutManager) mBinding.rvMessage.getLayoutManager()).scrollToPositionWithOffset(mViewModel.mMessageItemList.size() - Integer.parseInt(state.offset), 10);
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTextWatcher != null)
            mBinding.etInputMsg.removeTextChangedListener(mTextWatcher);
        if (mOnLayoutChangeListener != null)
            mBinding.rvMessage.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        mBinding.rvMessage.clearOnScrollListeners();
        mViewModel.mMessageItemList.clear();
        mTextWatcher = null;
        mOnLayoutChangeListener = null;
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

    private void fetchMessageList(Query query, int prevCnt, String prevCursor) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                if (mViewModel.mFirstMessageKey != null && mViewModel.mFirstMessageKey.equals(dataSnapshot.getKey()))
                    return;
                else if (s == null)
                    mViewModel.mFirstMessageKey = dataSnapshot.getKey();
                if (mViewModel.mCursor == null)
                    mViewModel.mCursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    mViewModel.mHasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);

                mViewModel.mMessageItemList.add(mViewModel.mMessageItemList.size() - prevCnt, messageItem); // 새로 추가하면 prevCnt는 0으로 됨
                mAdapter.notifyDataSetChanged();
                try {
                    if (mHasSelection || mViewModel.mHasRequestedMore)
                        if (prevCnt == 0)
                            mBinding.rvMessage.scrollToPosition(mViewModel.mMessageItemList.size() - 1);
                        else
                            ((LinearLayoutManager) mBinding.rvMessage.getLayoutManager()).scrollToPositionWithOffset(mViewModel.mMessageItemList.size() - prevCnt, 10);
                } catch (Exception e) {
                    Log.e("TEST", e.getMessage());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendLMSMessage() {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.SEND_MESSAGE, null, response -> {
            try {
                if (!response.getBoolean("isError"))
                    Log.d("채팅", response.getString("message"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> VolleyLog.e(error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("TXT", mBinding.etInputMsg.getText().toString());
                params.put("send_msg", "Y");
                params.put("USERS", mValue);
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        params.forEach((k, v) -> {
                            try {
                                encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                encodedParams.append('=');
                                encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                encodedParams.append('&');
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        });
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        }, "req_send_msg");
    }
}
