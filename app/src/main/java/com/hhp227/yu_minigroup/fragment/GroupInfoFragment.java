package com.hhp227.yu_minigroup.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.RequestActivity;
import com.hhp227.yu_minigroup.databinding.FragmentGroupInfoBinding;
import com.hhp227.yu_minigroup.viewmodel.GroupInfoViewModel;

// TODO
public class GroupInfoFragment extends DialogFragment {
    public static final int TYPE_REQUEST = 0;

    public static final int TYPE_CANCEL = 1;

    private static final int DESC_MAX_LINE = 6;

    private FragmentGroupInfoBinding mBinding;

    private GroupInfoViewModel mViewModel;

    public static GroupInfoFragment newInstance() {
        Bundle args = new Bundle();

        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
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
        mViewModel = new ViewModelProvider(this).get(GroupInfoViewModel.class);

        mBinding.bRequest.setOnClickListener(v -> mViewModel.sendRequest());
        mBinding.bClose.setOnClickListener(v -> dismiss());
        mBinding.tvName.setText(mViewModel.mGroupName);
        mBinding.tvInfo.setText(mViewModel.mGroupInfo);
        mBinding.tvDesciption.setText(mViewModel.mGroupDesc);
        mBinding.tvDesciption.setMaxLines(DESC_MAX_LINE);
        mBinding.bRequest.setText(mViewModel.mButtonType != null && mViewModel.mButtonType == TYPE_REQUEST ? "가입신청" : "신청취소");
        Glide.with(this)
                .load(mViewModel.mGroupImage)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(mBinding.ivGroupImage);
        mViewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {

            } else if (state.type >= 0) {
                Toast.makeText(getContext(), state.message, Toast.LENGTH_LONG).show();
                switch (state.type) {
                    case TYPE_REQUEST:
                        Intent intent = new Intent(getContext(), MainActivity.class);

                        if (getActivity() != null) {
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                        break;
                    case TYPE_CANCEL:
                        ((RequestActivity) requireActivity()).refresh();
                        GroupInfoFragment.this.dismiss();
                        break;
                }
            } else if (state.message != null && !state.message.isEmpty()) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
