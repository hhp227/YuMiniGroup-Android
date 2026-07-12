package com.hhp227.yu_minigroup.data;

import android.graphics.Bitmap;

import com.hhp227.yu_minigroup.data.remote.ArticleRemoteDataSource;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.Callback;

import java.util.List;

public class ArticleRepository {
    private final ArticleRemoteDataSource mArticleRemoteDataSource;

    public ArticleRepository(String groupId, String key) {
        this.mArticleRemoteDataSource = new ArticleRemoteDataSource(groupId, key);
    }

    public boolean isStopRequestMore() {
        return mArticleRemoteDataSource.isStopRequestMore();
    }

    public void setMinId(long minId) {
        mArticleRemoteDataSource.setMinId(minId);
    }

    public void getArticleList(String cookie, String params, Callback callback) {
        mArticleRemoteDataSource.getArticleList(cookie, params, callback);
    }

    public void getArticleData(String cookie, String articleId, String articleKey, String params, Callback callback) {
        mArticleRemoteDataSource.getArticleData(cookie, articleId, articleKey, params, callback);
    }

    public void addArticle(String cookie, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        mArticleRemoteDataSource.addArticle(cookie, user, title, content, imageList, youTubeItem, callback);
    }

    public void setArticle(String cookie, String articleId, String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        mArticleRemoteDataSource.setArticle(cookie, articleId, articleKey, title, content, imageList, youTubeItem, callback);
    }

    public void removeArticle(String cookie, String articleId, String articleKey, Callback callback) {
        mArticleRemoteDataSource.removeArticle(cookie, articleId, articleKey, callback);
    }

    public void addArticleImage(String cookie, Bitmap bitmap, Callback callback) {
        mArticleRemoteDataSource.addArticleImage(cookie, bitmap, callback);
    }
}
