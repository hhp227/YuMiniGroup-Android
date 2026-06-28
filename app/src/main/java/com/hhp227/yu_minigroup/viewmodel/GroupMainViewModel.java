package com.hhp227.yu_minigroup.viewmodel;

import android.os.CountDownTimer;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupMainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<Map.Entry<String, Object>>> mGroupItemList = new MutableLiveData<>();

    private final MutableLiveData<List<GroupItem>> mPopularGroupItemList = new MutableLiveData<>(Collections.emptyList());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<Long> mTick = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CountDownTimer mCountDownTimer = new CountDownTimer(80000, 8000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mTick.postValue(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            start();
        }
    };

    private final GroupRepository mGroupRepository = new GroupRepository();

    public GroupMainViewModel() {
        fetchDataTask();
    }

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<List<Map.Entry<String, Object>>> getItemList() {
        return mGroupItemList;
    }

    public LiveData<List<GroupItem>> getPopularItemList() {
        return mPopularGroupItemList;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void startCountDownTimer() {
        mCountDownTimer.start();
    }

    public void cancelCountDownTimer() {
        mCountDownTimer.cancel();
    }

    public LiveData<Long> getTick() {
        return mTick;
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mGroupRepository.getJoinedGroupList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), getUser(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, Object>> groupItemList = (List<Map.Entry<String, Object>>) data;

                mLoading.postValue(false);
                mGroupItemList.postValue(groupItemList);
                if (groupItemList.isEmpty()) {
                    fetchPopularGroupList();
                }
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
    }

    private void fetchPopularGroupList() {
        mGroupRepository.getPopularGroupList(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mLoading.postValue(false);
                mPopularGroupItemList.postValue((List<GroupItem>) data);
            }

            @Override
            public void onFailure(Throwable throwable) {
                mLoading.postValue(false);
                mPopularGroupItemList.postValue(Collections.emptyList());
                mMessage.postValue(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                mLoading.postValue(true);
            }
        });
    }
}