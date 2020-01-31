package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.*;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.User;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Tab4Fragment extends Fragment {
    private static final String TAG = "설정";
    private static boolean mIsAdmin;
    private static int mPosition;
    private static String mGroupId, mKey;
    private long mLastClickTime;
    private User mUser;

    public Tab4Fragment() {
    }

    public static Tab4Fragment newInstance(boolean isAdmin, String grpId, int position, String key) {
        Bundle args = new Bundle();
        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putInt("position", position);
        args.putString("key", key);
        Tab4Fragment fragment = new Tab4Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsAdmin = getArguments().getBoolean("admin");
            mGroupId = getArguments().getString("grp_id");
            mPosition = getArguments().getInt("position");
            mKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerView.Adapter<Tab4Holder>() {

            @Override
            public Tab4Holder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.content_tab4, parent, false);
                return new Tab4Holder(view);
            }

            @Override
            public void onBindViewHolder(Tab4Holder holder, int position) {
                mUser = AppController.getInstance().getPreferenceManager().getUser();
                String stuYuId = mUser.getUserId();
                String userName = mUser.getName();

                Glide.with(getContext())
                        .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUser.getUid()), new LazyHeaders.Builder().addHeader("Cookie", AppController.getInstance().getPreferenceManager().getCookie()).build()))
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.profileImage);
                holder.name.setText(userName);
                holder.yuId.setText(stuYuId);
                holder.profile.setOnClickListener(this::onClick);
                holder.withdrawal.setOnClickListener(this::onClick);
                if (mIsAdmin) {
                    holder.withdrawalText.setText("소모임 폐쇄");
                    holder.settings.setOnClickListener(this::onClick);
                    holder.settings.setVisibility(View.VISIBLE);
                } else {
                    holder.withdrawalText.setText("소모임 탈퇴");
                    holder.settings.setVisibility(View.GONE);
                }
                holder.feedback.setOnClickListener(this::onClick);
                holder.appStore.setOnClickListener(this::onClick);
                holder.share.setOnClickListener(this::onClick);
                holder.version.setOnClickListener(this::onClick);
                holder.adView.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public int getItemCount() {
                return 1;
            }

            private void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ll_profile:
                        startActivity(new Intent(getContext(), ProfileActivity.class));
                        break;
                    case R.id.ll_withdrawal:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage((mIsAdmin ? "폐쇄" : "탈퇴") + "하시겠습니까?");
                        builder.setPositiveButton("예", (dialog, which) -> {
                            AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, mIsAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, response -> {
                                try {
                                    if (!response.getBoolean("isError")) {
                                        Toast.makeText(getContext(), "소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    //hideProgressDialog();
                                    deleteGroupFromFirebase();
                                }
                            }, error -> {
                                VolleyLog.e(TAG, error.getMessage());
                                //hideProgressDialog();
                            }) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Cookie", AppController.getInstance().getPreferenceManager().getCookie());
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
                        });
                        builder.setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
                        builder.show();
                        break;
                    case R.id.ll_settings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        intent.putExtra("grp_id", mGroupId);
                        intent.putExtra("key", mKey);
                        startActivityForResult(intent, GroupFragment.UPDATE_GROUP);
                        break;
                    case R.id.ll_feedback:
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.setType("plain/Text");
                        email.putExtra(Intent.EXTRA_EMAIL, new String[] {"hong227@naver.com"});
                        email.putExtra(Intent.EXTRA_SUBJECT, "영남대소모임 건의사항");
                        email.putExtra(Intent.EXTRA_TEXT, "작성자 (Writer) : " + mUser.getName() + "\n기기 모델 (Model) : " + Build.MODEL + "\n앱 버전 (AppVer) : " + Build.VERSION.RELEASE + "\n내용 (Content) : " + "");
                        email.setType("message/rfc822");
                        startActivity(email);
                        break;
                    case R.id.ll_appstore:
                        String appUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                        break;
                    case R.id.ll_share:
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                                "GitHub Page :  https://localhost/" +
                                "Sample App : https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
                        startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                        break;
                    case R.id.ll_verinfo:
                        startActivity(new Intent(getActivity(), VerInfoActivity.class));
                        break;
                }
            }
        });

        return rootView;
    }

    private void deleteGroupFromFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        if (mIsAdmin) {
            groupsReference.child(mKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildren().forEach(snapshot -> userGroupListReference.child(snapshot.getKey()).child(mKey).removeValue());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                }
            });
            articlesReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");
                    dataSnapshot.getChildren().forEach(snapshot -> replysReference.child(snapshot.getKey()).removeValue());
                    articlesReference.child(mKey).removeValue();
                    groupsReference.child(mKey).removeValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                }
            });
        } else {
            groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);
                    if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(mUser.getUid())) {
                        Map<String, Boolean> members = groupItem.getMembers();
                        members.remove(mUser.getUid());
                        groupItem.setMembers(members);
                        groupItem.setMemberCount(members.size());
                    }
                    groupsReference.child(mKey).setValue(groupItem);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                }
            });
            userGroupListReference.child(AppController.getInstance().getPreferenceManager().getUser().getUid()).child(mKey).removeValue();
        }
    }

    public class Tab4Holder extends RecyclerView.ViewHolder {
        private AdView adView;
        private LinearLayout profile, withdrawal, settings, feedback, appStore, share, version;
        private ImageView profileImage;
        private TextView name, yuId, withdrawalText;

        public Tab4Holder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.ad_view);
            appStore = itemView.findViewById(R.id.ll_appstore);
            feedback = itemView.findViewById(R.id.ll_feedback);
            name = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.ll_profile);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            settings = itemView.findViewById(R.id.ll_settings);
            share = itemView.findViewById(R.id.ll_share);
            version = itemView.findViewById(R.id.ll_verinfo);
            withdrawal = itemView.findViewById(R.id.ll_withdrawal);
            withdrawalText = itemView.findViewById(R.id.tv_withdrawal);
            yuId = itemView.findViewById(R.id.tv_yu_id);
        }
    }
}
