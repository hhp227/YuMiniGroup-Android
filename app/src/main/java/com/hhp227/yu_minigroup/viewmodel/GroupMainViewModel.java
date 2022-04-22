package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.ArrayList;
import java.util.List;

public class GroupMainViewModel extends ViewModel {
    public final List<String> mGroupItemKeys = new ArrayList<>();

    public final List<Object> mGroupItemValues = new ArrayList<>();

    public void refresh() {
        mGroupItemKeys.clear();
        mGroupItemValues.clear();
    }

    public void test(String id, GroupItem groupItem) {
        mGroupItemKeys.add(id);
        mGroupItemValues.add(groupItem);
    }

}
