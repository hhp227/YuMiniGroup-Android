package com.hhp227.yu_minigroup.data;

import com.hhp227.yu_minigroup.data.remote.UserRemoteDataSource;
import com.hhp227.yu_minigroup.helper.Callback;

public class UserRepository {
    private final UserRemoteDataSource mUserRemoteDataSource;

    public UserRepository() {
        this.mUserRemoteDataSource = new UserRemoteDataSource();
    }

    public UserRepository(String mGroupKey) {
        this.mUserRemoteDataSource = new UserRemoteDataSource(mGroupKey);
    }

    public void getManagedMemberList(String cookie, String groupId, Callback callback) {
        mUserRemoteDataSource.getManagedMemberList(cookie, groupId, callback);
    }

    public void getUserList(int limit, Callback callback) {
        mUserRemoteDataSource.getUserList(limit, callback);
    }
}
