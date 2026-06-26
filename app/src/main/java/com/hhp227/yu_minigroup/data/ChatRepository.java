package com.hhp227.yu_minigroup.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.dto.MessageItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {
    private final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");

    public void fetchMessageList(String currentUserUid, String receiver, boolean isGroupChat, String cursor, int limit, Callback callback) {
        Query query = isGroupChat
                ? mDatabaseReference.child(receiver).orderByKey().limitToLast(limit)
                : mDatabaseReference.child(currentUserUid).child(receiver).orderByKey().limitToLast(limit);

        if (cursor != null) {
            query = query.endAt(cursor);
        }
        callback.onLoading();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Map.Entry<String, MessageItem>> messageItemList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    messageItemList.add(new AbstractMap.SimpleEntry<>(snapshot.getKey(), snapshot.getValue(MessageItem.class)));
                }
                callback.onSuccess(messageItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void sendMessage(User user, String receiver, boolean isGroupChat, String text) {
        Map<String, Object> map = new HashMap<>();

        map.put("from", user.getUid());
        map.put("name", user.getName());
        map.put("message", text);
        map.put("type", "text");
        map.put("seen", false);
        map.put("timestamp", System.currentTimeMillis());
        if (isGroupChat) {
            mDatabaseReference.child(receiver).push().setValue(map);
        } else {
            String receiverPath = receiver + "/" + user.getUid() + "/";
            String senderPath = user.getUid() + "/" + receiver + "/";
            String pushId = mDatabaseReference.child(user.getUid()).child(receiver).push().getKey();
            Map<String, Object> messageMap = new HashMap<>();

            if (pushId == null) {
                return;
            }
            messageMap.put(receiverPath.concat(pushId), map);
            messageMap.put(senderPath.concat(pushId), map);
            mDatabaseReference.updateChildren(messageMap);
        }
    }
}
