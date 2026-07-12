package com.hhp227.yu_minigroup.data;

import com.hhp227.yu_minigroup.data.remote.ReplyRemoteDataSource;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;

import java.util.List;

public class ReplyRepository {
    private final ReplyRemoteDataSource mReplyRemoteDataSource;

    public ReplyRepository(String groupId, String articleId, String articleKey) {
        this.mReplyRemoteDataSource = new ReplyRemoteDataSource(groupId, articleId, articleKey);
    }

    public void getReplyList(List<Element> commentList, Callback callback) {
        mReplyRemoteDataSource.getReplyList(commentList, callback);
    }

    public void addReply(String cookie, User user, String text, Callback callback) {
        mReplyRemoteDataSource.addReply(cookie, user, text, callback);
    }

    public void setReply(String cookie, String replyId, String replyKey, String text, Callback callback) {
        mReplyRemoteDataSource.setReply(cookie, replyId, replyKey, text, callback);
    }

    public void removeReply(String cookie, String replyId, String replyKey, Callback callback) {
        mReplyRemoteDataSource.removeReply(cookie, replyId, replyKey, callback);
    }
}
