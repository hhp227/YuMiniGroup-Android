package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
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
    private static final String TAG = UpdateReplyViewModel.class.getSimpleName(), STATE = "state", REPLY_FORM_STATE = "replyFormState";

    private final String mGroupId, mArticleId, mReplyId, mArticleKey, mReplyKey;

    private final SavedStateHandle mSavedStateHandle;

    private String mReply;

    public UpdateReplyViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
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

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ReplyFormState> getReplyFormState() {
        return mSavedStateHandle.getLiveData(REPLY_FORM_STATE);
    }

    public void actionSend(String text) {
        if (!TextUtils.isEmpty(text)) {
            String tag_string_req = "req_send";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_REPLY, response -> {
                try {
                    mSavedStateHandle.set(STATE, new State(false, response, null));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData(text);
                }
            }, error -> mSavedStateHandle.set(STATE, new State(false, null, error.getMessage()))) {
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

            mSavedStateHandle.set(STATE, new State(true, null, null));
            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        } else {
            mSavedStateHandle.set(REPLY_FORM_STATE, new ReplyFormState("내용을 입력하세요."));
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
                mSavedStateHandle.set(STATE, new State(false, null, databaseError.getMessage()));
            }
        });
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public String text;

        public String message;

        public State(boolean isLoading, String text, String message) {
            this.isLoading = isLoading;
            this.text = text;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            text = in.readString();
            message = in.readString();
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (isLoading ? 1 : 0));
            parcel.writeString(text);
            parcel.writeString(message);
        }
    }

    public static final class ReplyFormState implements Parcelable {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }

        protected ReplyFormState(Parcel in) {
            replyError = in.readString();
        }

        public static final Creator<ReplyFormState> CREATOR = new Creator<ReplyFormState>() {
            @Override
            public ReplyFormState createFromParcel(Parcel in) {
                return new ReplyFormState(in);
            }

            @Override
            public ReplyFormState[] newArray(int size) {
                return new ReplyFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(replyError);
        }
    }
}
