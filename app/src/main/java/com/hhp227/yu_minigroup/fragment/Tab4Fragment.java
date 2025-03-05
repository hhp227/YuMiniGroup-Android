package com.hhp227.yu_minigroup.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.GroupActivity;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.NoticeActivity;
import com.hhp227.yu_minigroup.activity.ProfileActivity;
import com.hhp227.yu_minigroup.activity.SettingsActivity;
import com.hhp227.yu_minigroup.activity.VerInfoActivity;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.ContentTab4Binding;
import com.hhp227.yu_minigroup.databinding.FragmentTab4Binding;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.viewmodel.Tab4ViewModel;

// TODO
public class Tab4Fragment extends Fragment {
    private FragmentTab4Binding mBinding;

    private Tab4ViewModel mViewModel;

    private ActivityResultLauncher<Intent> mProfileActivityResultLauncher, mSettingsActivityResultLauncher;

    public static Tab4Fragment newInstance(boolean isAdmin, String grpId, String grpImg, String key) {
        Tab4Fragment fragment = new Tab4Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab4Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(Tab4ViewModel.class);
        mProfileActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> ((GroupActivity) requireActivity()).onProfileActivityResult(result));
        mSettingsActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();

                if (data != null) {
                    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
                    String groupName = data.getStringExtra("grp_nm");
                    String groupDescription = data.getStringExtra("grp_desc");
                    String joinType = data.getStringExtra("join_div");
                    Intent intent = new Intent(getContext(), GroupMainFragment.class);

                    intent.putExtra("grp_nm", groupName);
                    intent.putExtra("grp_desc", groupDescription);
                    intent.putExtra("join_div", joinType);
                    requireActivity().setResult(Activity.RESULT_OK, intent);
                    if (actionBar != null) {
                        actionBar.setTitle(groupName);
                    }
                }
            }
        });

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.recyclerView.setAdapter(new RecyclerView.Adapter<Tab4Holder>() {
            @NonNull
            @Override
            public Tab4Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new Tab4Holder(ContentTab4Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull Tab4Holder holder, int position) {
                holder.bind(mViewModel.getUser());
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {

            } else if (state.isSuccess) {
                requireActivity().setResult(RESULT_OK, new Intent(getContext(), MainActivity.class));
                requireActivity().finish();
                Toast.makeText(getContext(), state.message, Toast.LENGTH_LONG).show();
            } else if (state.message != null && !state.message.isEmpty()) {
                Toast.makeText(getContext(), state.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mProfileActivityResultLauncher = null;
        mSettingsActivityResultLauncher = null;
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            mBinding.recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public class Tab4Holder extends RecyclerView.ViewHolder {
        private final ContentTab4Binding mBinding;

        public Tab4Holder(ContentTab4Binding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.llProfile.setOnClickListener(this::onClick);
            mBinding.llWithdrawal.setOnClickListener(this::onClick);
            mBinding.llNotice.setOnClickListener(this::onClick);
            mBinding.llFeedback.setOnClickListener(this::onClick);
            mBinding.llAppstore.setOnClickListener(this::onClick);
            mBinding.llShare.setOnClickListener(this::onClick);
            mBinding.llVerinfo.setOnClickListener(this::onClick);
            mBinding.llSettings.setOnClickListener(this::onClick);
        }

        public void bind(User user) {
            Glide.with(itemView.getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", mViewModel.getCookie())
                            .build()))
                    .apply(RequestOptions
                            .circleCropTransform()
                            .error(R.drawable.user_image_view_circle)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mBinding.ivProfileImage);
            mBinding.tvName.setText(user.getName());
            mBinding.tvYuId.setText(user.getUserId());

            if (mViewModel.mIsAdmin) {
                mBinding.tvWithdrawal.setText("소모임 폐쇄");
                mBinding.llSettings.setVisibility(View.VISIBLE);
            } else {
                mBinding.tvWithdrawal.setText("소모임 탈퇴");
                mBinding.llSettings.setVisibility(View.GONE);
            }
            mBinding.adView.loadAd(new AdRequest.Builder().build());
        }

        private void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_profile:
                    mProfileActivityResultLauncher.launch(new Intent(getContext(), ProfileActivity.class));
                    break;
                case R.id.ll_withdrawal:
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                    builder.setMessage((mViewModel.mIsAdmin ? "폐쇄" : "탈퇴") + "하시겠습니까?");
                    builder.setPositiveButton("예", (dialog, which) -> mViewModel.deleteGroup());
                    builder.setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
                    builder.show();
                    break;
                case R.id.ll_settings:
                    Intent intent = new Intent(getContext(), SettingsActivity.class);

                    intent.putExtra("grp_id", mViewModel.mGroupId);
                    intent.putExtra("grp_img", mViewModel.mGroupImage);
                    intent.putExtra("key", mViewModel.mKey);
                    mSettingsActivityResultLauncher.launch(intent);
                    break;
                case R.id.ll_notice:
                    startActivity(new Intent(getContext(), NoticeActivity.class));
                    break;
                case R.id.ll_feedback:
                    Intent email = new Intent(Intent.ACTION_SEND);

                    email.setType("plain/Text");
                    email.putExtra(Intent.EXTRA_EMAIL, new String[] {"hong227@naver.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "영남대소모임 건의사항");
                    email.putExtra(Intent.EXTRA_TEXT, "작성자 (Writer) : " + mViewModel.getUser().getName() + "\n기기 모델 (Model) : " + Build.MODEL + "\n앱 버전 (AppVer) : " + Build.VERSION.RELEASE + "\n내용 (Content) : " + "");
                    email.setType("message/rfc822");
                    startActivity(email);
                    break;
                case R.id.ll_appstore:
                    String appUrl = "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                    break;
                case R.id.ll_share:
                    Intent share = new Intent(Intent.ACTION_SEND);

                    share.setType("text/plain");
                    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                            "GitHub Page :  https://localhost/" +
                            "Sample App : https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
                    startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                    break;
                case R.id.ll_verinfo:
                    startActivity(new Intent(getActivity(), VerInfoActivity.class));
                    break;
            }
        }
    }
}
