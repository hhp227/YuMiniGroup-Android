package com.hhp227.yu_minigroup.dto;

public class YouTubeItem {
    public String videoId;
    public String publishedAt;
    public String title;
    public String thumbnail;
    public String channelTitle;

    public YouTubeItem(String videoId, String publishedAt, String title, String thumbnail, String channelTitle) {
        this.videoId = videoId;
        this.publishedAt = publishedAt;
        this.title = title;
        this.thumbnail = thumbnail;
        this.channelTitle = channelTitle;
    }
}