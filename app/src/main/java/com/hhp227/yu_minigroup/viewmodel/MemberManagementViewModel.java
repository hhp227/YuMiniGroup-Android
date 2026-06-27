package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.UserRepository;
import com.hhp227.yu_minigroup.dto.MemberItem;
import com.hhp227.yu_minigroup.helper.Callback;

import java.util.Collections;
import java.util.List;

public class MemberManagementViewModel extends ViewModel {
    private static final String GROUP_ID = "grp_id";

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<MemberItem>> mItemList = new MutableLiveData<>(Collections.emptyList());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final UserRepository mUserRepository = new UserRepository();

    private final String mGroupId;

    public MemberManagementViewModel(SavedStateHandle savedStateHandle) {
        mGroupId = savedStateHandle.get(GROUP_ID);

        fetchMemberList();
    }

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<List<MemberItem>> getItemList() {
        return mItemList;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public void refresh() {
        fetchMemberList();
    }

    private void fetchMemberList() {
        if (mGroupId != null) {
            mUserRepository.getManagedMemberList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mGroupId, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mLoading.postValue(false);
                    mItemList.postValue((List<MemberItem>) data);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    mLoading.postValue(false);
                    mMessage.postValue(throwable.getMessage());
                }

                @Override
                public void onLoading() {
                    mLoading.postValue(true);
                }
            });
        } else {
            mMessage.postValue("그룹 정보가 없습니다.");
        }
    }
}
