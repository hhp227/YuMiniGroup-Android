package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Tab4ViewModel extends ViewModel {
    public Boolean mIsAdmin;

    public String mGroupId, mGroupImage, mKey;

    private static final String TAG = Tab4ViewModel.class.getSimpleName(), STATE = "state";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    public Tab4ViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
        mIsAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupImage = savedStateHandle.get("grp_img");
        mKey = savedStateHandle.get("key");
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public void deleteGroup() {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, mIsAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, response -> {
            try {
                if (!response.getBoolean("isError")) {
                    deleteGroupFromFirebase();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mSavedStateHandle.set(STATE, new State(false, false, e.getMessage()));
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mSavedStateHandle.set(STATE, new State(false, false, error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
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
                throw new RuntimeException();
            }
        });
    }

    private void deleteGroupFromFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        if (mIsAdmin) {
            groupsReference.child(mKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildren().forEach(snapshot -> {
                        if (snapshot.getKey() != null) {
                            userGroupListReference.child(snapshot.getKey()).child(mKey).removeValue();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                    mSavedStateHandle.set(STATE, new State(false, false, databaseError.getMessage()));
                }
            });
            articlesReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

                    dataSnapshot.getChildren().forEach(snapshot -> {
                        if (snapshot.getKey() != null) {
                            replysReference.child(snapshot.getKey()).removeValue();
                        }
                    });
                    articlesReference.child(mKey).removeValue();
                    groupsReference.child(mKey).removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                    mSavedStateHandle.set(STATE, new State(false, false, databaseError.getMessage()));
                }
            });
        } else {
            groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                    if (groupItem != null) {
                        if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(getUser().getUid())) {
                            Map<String, Boolean> members = groupItem.getMembers();

                            members.remove(getUser().getUid());
                            groupItem.setMembers(members);
                            groupItem.setMemberCount(members.size());
                        }
                    }
                    groupsReference.child(mKey).setValue(groupItem);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                    mSavedStateHandle.set(STATE, new State(false, false, databaseError.getMessage()));
                }
            });
            userGroupListReference.child(getUser().getUid()).child(mKey).removeValue();
        }
        mSavedStateHandle.set(STATE, new State(false, true, "소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료"));
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public boolean isSuccess;

        public String message;

        public State(boolean isLoading, boolean isSuccess, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            isSuccess = in.readByte() != 0;
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
            parcel.writeByte((byte) (isSuccess ? 1 : 0));
            parcel.writeString(message);
        }
    }
}
