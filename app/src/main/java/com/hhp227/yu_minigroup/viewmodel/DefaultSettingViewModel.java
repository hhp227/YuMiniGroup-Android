package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.webkit.CookieManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.Callback;

public class DefaultSettingViewModel extends ViewModel {
    private static final String GROUP_ID = "grp_id";

    private static final String GROUP_IMAGE = "grp_img";

    private static final String GROUP_KEY = "key";

    public final MutableLiveData<String> title = new MutableLiveData<>("");

    public final MutableLiveData<String> description = new MutableLiveData<>("");

    public final MutableLiveData<Boolean> joinType = new MutableLiveData<>(true);

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<GroupItem> mGroupItem = new MutableLiveData<>();

    private final MutableLiveData<GroupItem> mUpdatedGroupItem = new MutableLiveData<>();

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<String> mTitleError = new MutableLiveData<>();

    private final MutableLiveData<String> mDescriptionError = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    private final MutableLiveData<String> mGroupImage = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    private final String mGroupId;

    private final String mGroupKey;

    public DefaultSettingViewModel(SavedStateHandle savedStateHandle) {
        mGroupId = savedStateHandle.get(GROUP_ID);
        mGroupKey = savedStateHandle.get(GROUP_KEY);
        String groupImage = savedStateHandle.get(GROUP_IMAGE);

        mGroupImage.setValue(groupImage);
        if (mGroupId != null) {
            loadGroupSetting(groupImage);
        }
    }

    public void setTitle(String title) {
        this.title.postValue(title);
    }

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<GroupItem> getGroupItem() {
        return mGroupItem;
    }

    public LiveData<GroupItem> getUpdatedGroupItem() {
        return mUpdatedGroupItem;
    }

    public LiveData<String> getMessage() {
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

    public LiveData<String> getGroupImage() {
        return mGroupImage;
    }

    public void updateGroup(String title, String description) {
        if (!title.isEmpty() && !description.isEmpty()) {
            String joinTypeValue = Boolean.TRUE.equals(joinType.getValue()) ? "0" : "1";

            mGroupRepository.setGroup(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mGroupKey, mGroupId, title, description, joinTypeValue, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mLoading.postValue(false);
                    mUpdatedGroupItem.postValue((GroupItem) data);
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
            mTitleError.postValue(title.isEmpty() ? "그룹이름을 입력하세요." : null);
            mDescriptionError.postValue(description.isEmpty() ? "그룹설명을 입력하세요." : null);
        }
    }

    private void loadGroupSetting(String groupImage) {
        mGroupRepository.getGroup(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mGroupId, groupImage, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                GroupItem groupItem = (GroupItem) data;

                mLoading.postValue(false);
                title.postValue(groupItem.getName());
                description.postValue(groupItem.getDescription());
                joinType.postValue("0".equals(groupItem.getJoinType()));
                mGroupItem.postValue(groupItem);
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
}
