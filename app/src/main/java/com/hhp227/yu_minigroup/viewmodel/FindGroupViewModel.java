package com.hhp227.yu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.GroupRepository;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

public class FindGroupViewModel extends ListViewModel<Map.Entry<String, GroupItem>> {
    private static final int LIMIT = 15;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    public FindGroupViewModel() {
        fetchNextPage();
    }

    public void fetchGroupList(int offset) {
        mGroupRepository.getNotJoinedGroupList(mCookieManager.getCookie(EndPoint.LOGIN_LMS), offset, LIMIT, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, GroupItem>> groupItemList = (List<Map.Entry<String, GroupItem>>) data;

                setLoading(false);
                setItemList(mergedList(getItemList().getValue(), groupItemList));
                setOffset(getOffset() + LIMIT);
                setEndReached(groupItemList.isEmpty());
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
                setRequestMore(offset > 1);
            }
        });
    }

    public void fetchNextPage() {
        setRequestMore(!mGroupRepository.isStopRequestMore());
        if (!mGroupRepository.isStopRequestMore()) {
            fetchGroupList(getOffset());
        }
    }

    public void refresh() {
        mGroupRepository.setMinId(0);
        Executors.newSingleThreadExecutor().execute(() -> {
            setOffset(1);
            setItemList(Collections.emptyList());
            setRequestMore(true);
            setEndReached(false);
            fetchGroupList(getOffset());
        });
    }

    private List<Map.Entry<String, GroupItem>> mergedList(List<Map.Entry<String, GroupItem>> existingList, List<Map.Entry<String, GroupItem>> newList) {
        return new ArrayList<Map.Entry<String, GroupItem>>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}