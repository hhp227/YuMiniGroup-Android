package com.hhp227.yu_minigroup.data;

import android.graphics.Bitmap;

import com.hhp227.yu_minigroup.data.remote.GroupRemoteDataSource;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;

public class GroupRepository {
    private final GroupRemoteDataSource mGroupRemoteDataSource = new GroupRemoteDataSource();

    public GroupRepository() {
    }

    public boolean isStopRequestMore() {
        return mGroupRemoteDataSource.isStopRequestMore();
    }

    public void setMinId(int minId) {
        mGroupRemoteDataSource.setMinId(minId);
    }

    public void getJoinedGroupList(String cookie, User user, Callback callback) {
        mGroupRemoteDataSource.getJoinedGroupList(cookie, user, callback);
    }

    public void getNotJoinedGroupList(String cookie, int offset, int limit, Callback callback) {
        mGroupRemoteDataSource.getNotJoinedGroupList(cookie, offset, limit, callback);
    }

    public void getJoinRequestGroupList(String cookie, User user, int offset, int limit, Callback callback) {
        mGroupRemoteDataSource.getJoinRequestGroupList(cookie, user, offset, limit, callback);
    }

    public void getPopularGroupList(String cookie, Callback callback) {
        mGroupRemoteDataSource.getPopularGroupList(cookie, callback);
    }

    public void getGroup(String cookie, String groupId, String groupImage, Callback callback) {
        mGroupRemoteDataSource.getGroup(cookie, groupId, groupImage, callback);
    }

    public void addGroup(String cookie, User user, Bitmap bitmap, String title, String description, String type, Callback callback) {
        mGroupRemoteDataSource.addGroup(cookie, user, bitmap, title, description, type, callback);
    }

    public void setGroup(String cookie, String groupKey, String groupId, String groupName, String description, String joinType, Callback callback) {
        mGroupRemoteDataSource.setGroup(cookie, groupKey, groupId, groupName, description, joinType, callback);
    }

    public void removeGroup(String cookie, User user, boolean isAdmin, String groupId, String key, Callback callback) {
        mGroupRemoteDataSource.removeGroup(cookie, user, isAdmin, groupId, key, callback);
    }
}
