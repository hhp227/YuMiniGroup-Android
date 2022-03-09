package com.hhp227.yu_minigroup.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.RequestActivity;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class GroupInfoFragment extends DialogFragment {
    public static final int TYPE_REQUEST = 0;

    public static final int TYPE_CANCEL = 1;

    private static final int DESC_MAX_LINE = 6;

    private static final String TAG = "정보창";

    private static int mButtonType;

    private static String mGroupId, mGroupName, mGroupImage, mGroupInfo, mGroupDesc, mJoinType, mKey;

    private CookieManager mCookieManager;

    private PreferenceManager mPreferenceManager;

    private FragmentGroupInfoBinding mBinding;

    public static GroupInfoFragment newInstance() {
        Bundle args = new Bundle();

        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getString("grp_id");
            mGroupName = getArguments().getString("grp_nm");
            mGroupImage = getArguments().getString("img");
            mGroupInfo = getArguments().getString("info");
            mGroupDesc = getArguments().getString("desc");
            mJoinType = getArguments().getString("type");
            mButtonType = getArguments().getInt("btn_type");
            mKey = getArguments().getString("key");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentGroupInfoBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookieManager = AppController.getInstance().getCookieManager();

        mBinding.bRequest.setOnClickListener(v -> {
            String tag_json_req = "req_register";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mButtonType == TYPE_REQUEST ? EndPoint.REGISTER_GROUP : EndPoint.WITHDRAWAL_GROUP, null, response -> {
                try {
                    if (mButtonType == TYPE_REQUEST && !response.getBoolean("isError")) {
                        Toast.makeText(getContext(), "신청완료", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getContext(), MainActivity.class);

                        if (getActivity() != null) {
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                        insertGroupToFirebase();
                    } else if (mButtonType == TYPE_CANCEL && !response.getBoolean("isError")) {
                        Toast.makeText(getContext(), "신청취소", Toast.LENGTH_LONG).show();
                        ((RequestActivity) getActivity()).refresh();
                        GroupInfoFragment.this.dismiss();
                        deleteUserInGroupFromFirebase();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> Log.e(TAG, error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
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
            };
            AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_req);
        });
        mBinding.bClose.setOnClickListener(v -> dismiss());
        mBinding.tvName.setText(mGroupName);
        mBinding.tvInfo.setText(mGroupInfo);
        mBinding.tvDesciption.setText(mGroupDesc);
        mBinding.tvDesciption.setMaxLines(DESC_MAX_LINE);
        mBinding.bRequest.setText(mButtonType == TYPE_REQUEST ? "가입신청" : "신청취소");
        Glide.with(this)
                .load(mGroupImage)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(mBinding.ivGroupImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void insertGroupToFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);
                Map<String, Boolean> members = groupItem.getMembers() != null && !groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid()) ? groupItem.getMembers() : new HashMap<String, Boolean>();

                members.put(mPreferenceManager.getUser().getUid(), mJoinType.equals("0"));
                groupItem.setMembers(members);
                groupItem.setMemberCount(members.size());
                groupsReference.child(mKey).setValue(groupItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/" + mPreferenceManager.getUser().getUid() + "/" + mKey, mJoinType.equals("0"));
        userGroupListReference.updateChildren(childUpdates);
    }

    private void deleteUserInGroupFromFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid())) {
                    Map<String, Boolean> members = groupItem.getMembers();

                    members.remove(mPreferenceManager.getUser().getUid());
                    groupItem.setMembers(members);
                    groupItem.setMemberCount(members.size());
                }
                groupsReference.child(mKey).setValue(groupItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
        userGroupListReference.child(mPreferenceManager.getUser().getUid()).child(mKey).removeValue();
    }
}
