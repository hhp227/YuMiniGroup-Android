package com.hhp227.yu_minigroup.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ReplyItem;

import java.util.HashMap;
import java.util.Map;

public class UpdateReplyViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    private static final String TAG = UpdateReplyViewModel.class.getSimpleName();

    private final String mGroupId, mArticleId, mReplyId, mArticleKey, mReplyKey;

    private String mReply;

    public UpdateReplyViewModel(SavedStateHandle savedStateHandle) {
        mGroupId = savedStateHandle.get("grp_id");
        mArticleId = savedStateHandle.get("artl_num");
        mReplyId = savedStateHandle.get("cmmt_num");
        mArticleKey = savedStateHandle.get("artl_key");
        mReplyKey = savedStateHandle.get("cmmt_key");
        mReply = savedStateHandle.get("cmt");

        if (mReply != null) {
            mReply = mReply.contains("※") ? mReply.substring(0, mReply.lastIndexOf("※")).trim() : mReply;
        }
    }

    public String getReply() {
        return mReply;
    }

    public void actionSend(String text) {
        if (!TextUtils.isEmpty(text)) {
            String tag_string_req = "req_send";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_REPLY, response -> {
                try {
                    mState.postValue(new State(false, response, null, null));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData(text);
                }
            }, error -> mState.postValue(new State(false, null, null, error.getMessage()))) {
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

            mState.postValue(new State(true, null, null, null));
            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        } else {
            mState.postValue(new State(false, null, new ReplyFormState("내용을 입력하세요."), null));
        }
    }

    private void initFirebaseData(String text) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        updateReplyDataToFirebase(databaseReference.child(mArticleKey).child(mReplyKey), text);
    }

    private void updateReplyDataToFirebase(final Query query, String text) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);

                    if (replyItem != null) {
                        replyItem.setReply(text + "\n");
                    }
                    query.getRef().setValue(replyItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mState.postValue(new State(false, null, null, databaseError.getMessage()));
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public String text;

        public ReplyFormState replyFormState;

        public String message;

        public State(boolean isLoading, String text, ReplyFormState replyFormState, String message) {
            this.isLoading = isLoading;
            this.text = text;
            this.replyFormState = replyFormState;
            this.message = message;
        }
    }

    public static final class ReplyFormState {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }
    }
}
