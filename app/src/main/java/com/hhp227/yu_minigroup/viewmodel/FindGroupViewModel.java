package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.ViewModel;

import com.hhp227.yu_minigroup.dto.GroupItem;

import java.util.ArrayList;
import java.util.List;

public class FindGroupViewModel extends ViewModel {
    private static final int LIMIT = 15;

    private final List<String> mGroupItemKeys = new ArrayList<>();

    private final List<GroupItem> mGroupItemValues = new ArrayList<>();

    public void fetchGroupList(int offset) {

    }

    public void fetchNextPage() {

    }

    public void refresh() {

    }

    public static final class State {
        public boolean isLoading;

        public boolean isSuccess;

        public int offset;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, boolean isSuccess, int offset, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }
}
