package com.hhp227.yu_minigroup.dto;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class MessageItem implements Serializable {
    private String from;

    private String name;

    private String message;

    private String type;

    private boolean seen;

    private long timestamp;

    public MessageItem() {
    }

    public MessageItem(String from, String name, String message, String type, boolean seen, long timestamp) {
        this.from = from;
        this.name = name;
        this.message = message;
        this.type = type;
        this.seen = seen;
        this.timestamp = timestamp;
    }

    @PropertyName("from")
    public String getFrom() {
        return from;
    }

    @PropertyName("from")
    public void setFrom(String from) {
        this.from = from;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("message")
    public String getMessage() {
        return message;
    }

    @PropertyName("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("seen")
    public boolean isSeen() {
        return seen;
    }

    @PropertyName("seen")
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}