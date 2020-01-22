package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.ProfileActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.SettingsActivity;
import com.hhp227.yu_minigroup.VerInfoActivity;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.User;

public class Tab4Fragment extends Fragment {
    private static boolean mIsAdmin;
    private static int mPosition;
    private static String mGroupId, mKey;
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
        recyclerView.setAdapter(new RecyclerView.Adapter<Tab4ViewHolder>() {

            @Override
            public Tab4ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.content_tab4, parent, false);
                return new Tab4ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(Tab4ViewHolder holder, int position) {
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
                holder.appStore.setOnClickListener(this::onClick);
                holder.share.setOnClickListener(this::onClick);
                holder.version.setOnClickListener(this::onClick);
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

    public class Tab4ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout profile, withdrawal, settings, appStore, share, version;
        private ImageView profileImage;
        private TextView name, yuId, withdrawalText;

        public Tab4ViewHolder(View itemView) {
            super(itemView);
            appStore = itemView.findViewById(R.id.ll_appstore);
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
