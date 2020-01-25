package com.hhp227.yu_minigroup.dto;

public class ReplyItem {
    private long timestamp;
    private boolean auth;
    private String id, uid, name, date, reply;

    public ReplyItem() {
    }

    public ReplyItem(String id, String uid, String name, String date, String reply, boolean auth, long timestamp) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.date = date;
        this.reply = reply;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
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