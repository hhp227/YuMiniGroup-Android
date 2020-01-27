package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
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
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        ImageView profileImage = rootView.findViewById(R.id.iv_profile_image);
        TextView userName = rootView.findViewById(R.id.tv_name);
        Button send = rootView.findViewById(R.id.b_send);
        Button close = rootView.findViewById(R.id.b_close);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUid = bundle.getString("uid");
            mName = bundle.getString("name");
            mValue = bundle.getString("value");
        }
        Glide.with(getActivity()).load(EndPoint.USER_IMAGE.replace("{UID}", mUid)).apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop()).into(profileImage);
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
        return rootView;
    }

}
