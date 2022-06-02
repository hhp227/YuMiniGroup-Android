package com.hhp227.yu_minigroup.viewmodel;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final List<MessageItem> mMessageItemList = new ArrayList<>();

    public Boolean mIsGroupChat;

    public String mReceiver;

    // temp

    public String mFirstMessageKey;

    public String mCursor;

    public boolean mHasRequestedMore;

    private final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    public ChatViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mIsGroupChat = savedStateHandle.get("grp_chat");
        mReceiver = savedStateHandle.get("uid");

        Log.e("TEST", "ChatViewModel init");
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public LiveData<InputMessageFormState> getMessageFormState() {
        return mSavedStateHandle.getLiveData("messageFormState");
    }

    @SuppressLint("todo")
    public void fetchMessageList(Query query, int prevCnt, String prevCursor) {
        mState.postValue(new State(true, Collections.emptyList(), null, false, null));
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
                mState.postValue(new State(false, Collections.emptyList(), String.valueOf(prevCnt), true, null)); // 임시로 hasRequestedMore에다 flag를 줌
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

    public void actionSend(String text) {
        if (!text.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            String sender = getUser().getUid();
            String name = getUser().getName();

            map.put("from", sender);
            map.put("name", name);
            map.put("message", text);
            map.put("type", "text");
            map.put("seen", false);
            map.put("timestamp", System.currentTimeMillis());
            if (mIsGroupChat) {
                mDatabaseReference.child(mReceiver).push().setValue(map);
            } else {
                String receiverPath = mReceiver + "/" + sender + "/";
                String senderPath = sender + "/" + mReceiver + "/";
                String pushId = mDatabaseReference.child(sender).child(mReceiver).push().getKey();
                Map<String, Object> messageMap = new HashMap<>();

                if (pushId != null) {
                    messageMap.put(receiverPath.concat(pushId), map);
                    messageMap.put(senderPath.concat(pushId), map);
                }
                mDatabaseReference.updateChildren(messageMap);
            }
            /*if (!mIsGroupChat)
                sendLMSMessage();*/
        } else {
            mSavedStateHandle.set("messageFormState", new InputMessageFormState("메시지를 입력하세요."));
        }
    }

    public static final class State {
        public boolean isLoading;

        public List<MessageItem> messageItemList;

        public String offset;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, List<MessageItem> messageItemList, String offset, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.messageItemList = messageItemList;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "ChatViewModel onCleared");
    }

    public static final class InputMessageFormState implements Parcelable {
        public String messageError;

        public InputMessageFormState(String messageError) {
            this.messageError = messageError;
        }

        protected InputMessageFormState(Parcel in) {
            messageError = in.readString();
        }

        public static final Creator<InputMessageFormState> CREATOR = new Creator<InputMessageFormState>() {
            @Override
            public InputMessageFormState createFromParcel(Parcel in) {
                return new InputMessageFormState(in);
            }

            @Override
            public InputMessageFormState[] newArray(int size) {
                return new InputMessageFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(messageError);
        }
    }
}
