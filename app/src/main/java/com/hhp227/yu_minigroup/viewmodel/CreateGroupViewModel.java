package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.webkit.CookieManager;

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
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final MutableLiveData<CreateGroupFormState> mCreateGroupFormState = new MutableLiveData<>();

    public final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    private String mType;

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
    }

    public void setJoinType(boolean joinType) {
        this.mType = !joinType ? "0" : "1";
    }

    public void createGroup(String title, String description) {
        if (!title.isEmpty() && !description.isEmpty()) {
            mGroupRepository.addGroup(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mPreferenceManager.getUser(), mBitmap.getValue(), title, description, mType, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mState.postValue(new State(false, (Map.Entry<String, GroupItem>) data, null));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    mState.postValue(new State(false, null, throwable.getMessage()));
                }

                @Override
                public void onLoading() {
                    mState.postValue(new State(true, null, null));
                }
            });
        } else {
            mCreateGroupFormState.postValue(new CreateGroupFormState(title.isEmpty() ? "그룹명을 입력하세요." : null, description.isEmpty() ? "그룹설명을 입력하세요." : null));
        }
    }

    public static final class State {
        public boolean isLoading;

        public Map.Entry<String, GroupItem> groupItemEntry;

        public String message;

        public State(boolean isLoading, Map.Entry<String, GroupItem> groupItemEntry, String message) {
            this.isLoading = isLoading;
            this.groupItemEntry = groupItemEntry;
            this.message = message;
        }
    }

    public static final class CreateGroupFormState {
        public String titleError;

        public String descriptionError;

        public CreateGroupFormState(String titleError, String descriptionError) {
            this.titleError = titleError;
            this.descriptionError = descriptionError;
        }
    }
}
