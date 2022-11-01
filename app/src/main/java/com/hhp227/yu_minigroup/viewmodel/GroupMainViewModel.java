package com.hhp227.yu_minigroup.viewmodel;

import android.os.CountDownTimer;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupMainViewModel extends ViewModel {
    private final MutableLiveData<Long> mTick = new MutableLiveData<>();

    private final MutableLiveData<State> mState = new MutableLiveData<>();

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

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mGroupRepository.getJoinedGroupList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), getUser(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mState.postValue(new State(false, (List<Map.Entry<String, Object>>) data, null));
            }

            @Override
            public void onFailure(Throwable throwable) {
                mState.postValue(new State(false, Collections.emptyList(), throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mState.postValue(new State(true, Collections.emptyList(), null));
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, Object>> groupItemList;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, Object>> groupItemList, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.message = message;
        }
    }
}
