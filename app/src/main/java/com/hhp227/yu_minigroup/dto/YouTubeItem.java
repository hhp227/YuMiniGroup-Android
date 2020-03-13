package com.hhp227.yu_minigroup.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class YouTubeItem implements Parcelable {
    public int position;
    public String videoId;
    public String publishedAt;
    public String title;
    public String thumbnail;
    public String channelTitle;

    public YouTubeItem() {
    }

    public YouTubeItem(String videoId, String publishedAt, String title, String thumbnail, String channelTitle) {
        this.videoId = videoId;
        this.publishedAt = publishedAt;
        this.title = title;
        this.thumbnail = thumbnail;
        this.channelTitle = channelTitle;
    }

    protected YouTubeItem(Parcel in) {
        position = in.readInt();
        videoId = in.readString();
        publishedAt = in.readString();
        title = in.readString();
        thumbnail = in.readString();
        channelTitle = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(position);
        dest.writeString(videoId);
        dest.writeString(publishedAt);
        dest.writeString(title);
        dest.writeString(thumbnail);
        dest.writeString(channelTitle);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<YouTubeItem> CREATOR = new Creator<YouTubeItem>() {
        @Override
        public YouTubeItem createFromParcel(Parcel in) {
            return new YouTubeItem(in);
        }

        @Override
        public YouTubeItem[] newArray(int size) {
            return new YouTubeItem[size];
        }
    };
}