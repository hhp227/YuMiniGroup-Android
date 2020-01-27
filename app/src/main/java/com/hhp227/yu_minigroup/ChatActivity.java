package com.hhp227.yu_minigroup;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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

/*
And it works the same as set android:transcriptMode="alwaysScroll" to ListView.
recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
            int oldBottom) {
        if (bottom < oldBottom) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            });
        }
    }
});
 */
public class ChatActivity extends AppCompatActivity {
    private static final int LIMIT = 20;
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().limitToLast(LIMIT), 0, "");
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
                if (mCursor == null)
                    mCursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    mHasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                mMessageItemList.add(mMessageItemList.size() - prevCnt, messageItem);
                mAdapter.notifyItemRangeChanged(mMessageItemList.size() > 1 ? mMessageItemList.size() - 2 : 0, 2);
                /*if (mHasSelection || mHasRequestedMore)
                    mListView.setSelection(prevCnt == 0 ? mMessageItemList.size() : mMessageItemList.size() - prevCnt + 1);*/
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
