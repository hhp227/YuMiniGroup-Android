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

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int LIMIT = 30;

    private boolean mHasRequestedMore, mHasSelection, mIsGroupChat;

    private DatabaseReference mDatabaseReference;

    private List<MessageItem> mMessageItemList;

    private MessageListAdapter mAdapter;

    private String mCursor, mSender, mReceiver, mValue, mFirstMessageKey;

    private TextWatcher mTextWatcher;

    private User mUser;

    private View.OnLayoutChangeListener mOnLayoutChangeListener;

    private ActivityChatBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        mMessageItemList = new ArrayList<>();
        mUser = AppController.getInstance().getPreferenceManager().getUser();
        mSender = mUser.getUid();
        mReceiver = getIntent().getStringExtra("uid");
        mValue = getIntent().getStringExtra("value");
        mIsGroupChat = getIntent().getBooleanExtra("grp_chat", false);
        mAdapter = new MessageListAdapter(mMessageItemList, mSender);
        mOnLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom && mHasSelection)
                mBinding.rvMessage.post(() -> mBinding.rvMessage.scrollToPosition(mMessageItemList.size() - 1));
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
            if (!TextUtils.isEmpty(mBinding.etInputMsg.getText().toString().trim())) {
                sendMessage();
                if (!mIsGroupChat)
                    sendLMSMessage();
                mBinding.etInputMsg.setText("");
            } else
                Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
        });
        mBinding.etInputMsg.addTextChangedListener(mTextWatcher);
        mAdapter.setHasStableIds(true);
        layoutManager.setStackFromEnd(true);
        mBinding.rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mBinding.rvMessage.canScrollVertically(-1) && !mHasRequestedMore && mCursor != null) {
                    mHasRequestedMore = true;

                    fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT), mMessageItemList.size(), mCursor);
                    mCursor = null;
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
        fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().limitToLast(LIMIT), 0, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTextWatcher != null)
            mBinding.etInputMsg.removeTextChangedListener(mTextWatcher);
        if (mOnLayoutChangeListener != null)
            mBinding.rvMessage.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        mBinding.rvMessage.clearOnScrollListeners();
        mMessageItemList.clear();
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
                if (mFirstMessageKey != null && mFirstMessageKey.equals(dataSnapshot.getKey()))
                    return;
                else if (s == null)
                    mFirstMessageKey = dataSnapshot.getKey();
                if (mCursor == null)
                    mCursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    mHasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);

                mMessageItemList.add(mMessageItemList.size() - prevCnt, messageItem); // 새로 추가하면 prevCnt는 0으로 됨
                mAdapter.notifyDataSetChanged();
                //mAdapter.notifyItemRangeChanged(mMessageItemList.size() > 1 ? mMessageItemList.size() - 2 : 0, 2);
                if (mHasSelection || mHasRequestedMore)
                    if (prevCnt == 0)
                        mBinding.rvMessage.scrollToPosition(mMessageItemList.size() - 1);
                    else
                        ((LinearLayoutManager) mBinding.rvMessage.getLayoutManager()).scrollToPositionWithOffset(mMessageItemList.size() - prevCnt, 10);
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

    private void sendMessage() {
        Map<String, Object> map = new HashMap<>();

        map.put("from", mSender);
        map.put("name", mUser.getName());
        map.put("message", mBinding.etInputMsg.getText().toString());
        map.put("type", "text");
        map.put("seen", false);
        map.put("timestamp", System.currentTimeMillis());
        if (mIsGroupChat) {
            mDatabaseReference.child(mReceiver).push().setValue(map);
        } else {
            String receiverPath = mReceiver + "/" + mSender + "/";
            String senderPath = mSender + "/" + mReceiver + "/";
            String pushId = mDatabaseReference.child(mSender).child(mReceiver).push().getKey();
            Map<String, Object> messageMap = new HashMap<>();

            if (pushId != null) {
                messageMap.put(receiverPath.concat(pushId), map);
                messageMap.put(senderPath.concat(pushId), map);
            }
            mDatabaseReference.updateChildren(messageMap);
        }
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
