package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.adapter.MessageListAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.dto.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int LIMIT = 10;
    private boolean mHasRequestedMore, mHasSelection, mIsGroupChat;
    private CardView mButtonSend;
    private DatabaseReference mDatabaseReference;
    private EditText mInputMessage;
    private List<MessageItem> mMessageItemList;
    private MessageListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private String mCursor, mSender, mReceiver, mValue;
    private TextView mSendText;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mButtonSend = findViewById(R.id.cv_btn_send);
        mInputMessage = findViewById(R.id.et_input_msg);
        mRecyclerView = findViewById(R.id.rv_message);
        mSendText = findViewById(R.id.tv_btn_send);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        mMessageItemList = new ArrayList<>();
        mUser = AppController.getInstance().getPreferenceManager().getUser();
        mSender = mUser.getUid();
        mReceiver = intent.getStringExtra("uid");
        mValue = intent.getStringExtra("value");
        mIsGroupChat = intent.getBooleanExtra("grp_chat", false);
        mAdapter = new MessageListAdapter(this, mMessageItemList, mSender);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mButtonSend.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(mInputMessage.getText().toString().trim())) {
                sendMessage();
                if (!mIsGroupChat)
                    sendLMSMessage();
                mInputMessage.setText("");
            } else
                Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
        });
        mInputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mButtonSend.setCardBackgroundColor(getResources().getColor(s.length() > 0 ? R.color.colorAccent : androidx.cardview.R.color.cardview_light_background, null));
                mSendText.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray, null));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mAdapter.setHasStableIds(true);
        layoutManager.setStackFromEnd(true);

        // 임시로 뺌
        /*mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1) && !mHasRequestedMore) {
                    mHasRequestedMore = true;
                    //fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT), mMessageItemList.size(), mCursor);
                    //mCursor = null;
                    Toast.makeText(getApplicationContext(), "mHasRequestedMore : " + mHasRequestedMore, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //mHasSelection = layoutManager.findFirstCompletelyVisibleItemPosition() + layoutManager.getChildCount() > layoutManager.getItemCount() - 20;
            }
        });*/
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByValue().limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByValue().limitToLast(LIMIT), 0, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*Log.e("1", "s : " + s + ", mCursor" + mCursor);
                if (mCursor == null)
                    mCursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    mHasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                mMessageItemList.add(mMessageItemList.size() - prevCnt, messageItem); // 새로 추가하면 prevCnt는 0으로 됨
                mAdapter.notifyItemRangeChanged(mMessageItemList.size() > 1 ? mMessageItemList.size() - 2 : 0, 2);
                Log.e("테스트", "size : " + mMessageItemList.size() + ", prevCnt : " + prevCnt + ", mCursor" + mCursor);
                if (mHasSelection || mHasRequestedMore)
                    mRecyclerView.scrollToPosition(prevCnt == 0 ? mMessageItemList.size() - 1 : mMessageItemList.size() - prevCnt);*/
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                mMessageItemList.add(messageItem);
                mAdapter.notifyItemInserted(mMessageItemList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("from", mSender);
        map.put("name", mUser.getName());
        map.put("message", mInputMessage.getText().toString());
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
            messageMap.put(receiverPath.concat(pushId), map);
            messageMap.put(senderPath.concat(pushId), map);

            mDatabaseReference.updateChildren(messageMap);
        }
    }

    private void sendLMSMessage() {
    }
}
