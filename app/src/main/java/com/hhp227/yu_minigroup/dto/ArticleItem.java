package com.hhp227.yu_minigroup.dto;

import java.util.List;

public class ArticleItem {
    private boolean auth;
    private long timestamp;
    private String id, uid, name, title, content, replyCount;
    private List<String> images;
    private YouTubeItem youtube;

    public ArticleItem() {
    }

    public ArticleItem(String id, String uid, String name, String title, String content, List<String> images, YouTubeItem youTube, String replyCount, boolean auth, long timestamp) {
        super();
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.title = title;
        this.content = content;
        this.images = images;
        this.youtube = youTube;
        this.replyCount = replyCount;
        this.auth = auth;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public YouTubeItem getYoutube() {
        return youtube;
    }

    public void setYoutube(YouTubeItem youtube) {
        this.youtube = youtube;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
