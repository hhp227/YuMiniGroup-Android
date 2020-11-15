package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

public class UserFragment extends DialogFragment {
    private String mUid, mName, mValue;

    public UserFragment() {
    }

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView profileImage = view.findViewById(R.id.iv_profile_image);
        TextView userName = view.findViewById(R.id.tv_name);
        Button send = view.findViewById(R.id.b_send);
        Button close = view.findViewById(R.id.b_close);
        Bundle bundle = getArguments();

        if (bundle != null) {
            mUid = bundle.getString("uid");
            mName = bundle.getString("name");
            mValue = bundle.getString("value");
        }
        Glide.with(this)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUid), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS))
                        .build()))
                .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(profileImage);
        userName.setText(mName);
        if (mUid.equals(AppController.getInstance().getPreferenceManager().getUser().getUid()))
            send.setVisibility(View.GONE);
        else {
            send.setText("메시지 보내기");
            send.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ChatActivity.class);

                intent.putExtra("grp_chat", false);
                intent.putExtra("chat_nm", mName);
                intent.putExtra("uid", mUid);
                intent.putExtra("value", mValue);
                startActivity(intent);
            });
        }
        close.setOnClickListener(v -> dismiss());
    }
}
