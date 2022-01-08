package com.hhp227.yu_minigroup.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.ChatActivity;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentUserBinding;

public class UserDialogFragment extends DialogFragment {
    private String mUid, mName, mValue;

    private FragmentUserBinding mBinding;

    public UserDialogFragment() {
    }

    public static UserDialogFragment newInstance() {
        return new UserDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUid = getArguments().getString("uid");
            mName = getArguments().getString("name");
            mValue = getArguments().getString("value");
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
        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Glide.with(this)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUid), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                        .build()))
                .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mBinding.ivProfileImage);
        mBinding.tvName.setText(mName);
        if (mUid.equals(AppController.getInstance().getPreferenceManager().getUser().getUid()))
            mBinding.bSend.setVisibility(View.GONE);
        else {
            mBinding.bSend.setText("메시지 보내기");
            mBinding.bSend.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ChatActivity.class);

                intent.putExtra("grp_chat", false);
                intent.putExtra("chat_nm", mName);
                intent.putExtra("uid", mUid);
                intent.putExtra("value", mValue);
                startActivity(intent);
            });
        }
        mBinding.bClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
