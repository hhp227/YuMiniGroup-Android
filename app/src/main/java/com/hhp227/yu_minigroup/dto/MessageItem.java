package com.hhp227.yu_minigroup.dto;

public class MessageItem {
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(long timestamp) {
        this.timestamp = timestamp;
    }
}