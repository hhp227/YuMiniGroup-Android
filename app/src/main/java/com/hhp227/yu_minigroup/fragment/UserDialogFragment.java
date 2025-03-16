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

import androidx.lifecycle.ViewModelProvider;
import com.hhp227.yu_minigroup.activity.ChatActivity;
import com.hhp227.yu_minigroup.databinding.FragmentUserBinding;
import com.hhp227.yu_minigroup.handler.OnFragmentUserDialogEventListener;
import com.hhp227.yu_minigroup.viewmodel.UserViewModel;

public class UserDialogFragment extends DialogFragment implements OnFragmentUserDialogEventListener {
    private UserViewModel mViewModel;

    private FragmentUserBinding mBinding;

    public static UserDialogFragment newInstance() {
        return new UserDialogFragment();
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
        mViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onSendClick() {
        Intent intent = new Intent(getContext(), ChatActivity.class);

        intent.putExtra("grp_chat", false);
        intent.putExtra("chat_nm", mViewModel.mName);
        intent.putExtra("uid", mViewModel.mUid);
        intent.putExtra("value", mViewModel.mValue);
        startActivity(intent);
    }

    @Override
    public void onCancelClick() {
        dismiss();
    }
}
