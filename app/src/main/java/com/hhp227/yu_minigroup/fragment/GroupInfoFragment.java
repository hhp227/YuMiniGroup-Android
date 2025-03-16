package com.hhp227.yu_minigroup.fragment;

import static android.app.Activity.RESULT_OK;
import static com.hhp227.yu_minigroup.viewmodel.GroupInfoViewModel.TYPE_CANCEL;
import static com.hhp227.yu_minigroup.viewmodel.GroupInfoViewModel.TYPE_REQUEST;

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

import com.hhp227.yu_minigroup.activity.MainActivity;
import com.hhp227.yu_minigroup.activity.RequestActivity;
import com.hhp227.yu_minigroup.databinding.FragmentGroupInfoBinding;
import com.hhp227.yu_minigroup.viewmodel.GroupInfoViewModel;

public class GroupInfoFragment extends DialogFragment {
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
        mViewModel = new ViewModelProvider(this).get(GroupInfoViewModel.class);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setOnClose(this::dismiss);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void observeViewModelData() {
        mViewModel.getType().observe(getViewLifecycleOwner(), type -> {
            switch (type) {
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
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
