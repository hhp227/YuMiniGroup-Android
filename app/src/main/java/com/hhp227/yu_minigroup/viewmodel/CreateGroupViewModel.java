package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import java.util.Map;

public class CreateGroupViewModel extends ViewModel {
    public final MutableLiveData<String> title = new MutableLiveData<>("");

    public final MutableLiveData<String> description = new MutableLiveData<>("");

    public final MutableLiveData<Boolean> joinType = new MutableLiveData<>(true);

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<Map.Entry<String, GroupItem>> mGroupItemEntry = new MutableLiveData<>();

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<String> mTitleError = new MutableLiveData<>();

    private final MutableLiveData<String> mDescriptionError = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    public void setTitle(String title) {
        this.title.postValue(title);
    }

    public MutableLiveData<Boolean> isLoading() {
        return mLoading;
    }

    public MutableLiveData<Map.Entry<String, GroupItem>> getGroupItemEntry() {
        return mGroupItemEntry;
    }

    public MutableLiveData<String> getMessage() {
        return mMessage;
    }

    public LiveData<String> getTitleError() {
        return mTitleError;
    }

    public LiveData<String> getDescriptionError() {
        return mDescriptionError;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
    }

    public LiveData<Bitmap> getBitmap() {
        return mBitmap;
    }

    public void createGroup(String title, String description) {
        if (!title.isEmpty() && !description.isEmpty()) {
            mGroupRepository.addGroup(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mPreferenceManager.getUser(), mBitmap.getValue(), title, description, Boolean.TRUE.equals(joinType.getValue()) ? "0" : "1", new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mLoading.postValue(false);
                    mGroupItemEntry.postValue((Map.Entry<String, GroupItem>) data);
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
            mTitleError.postValue(title.isEmpty() ? "그룹명을 입력하세요." : null);
            mDescriptionError.postValue(description.isEmpty() ? "그룹설명을 입력하세요." : null);
        }
    }
}
