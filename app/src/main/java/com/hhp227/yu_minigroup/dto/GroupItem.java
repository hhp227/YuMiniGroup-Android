package com.hhp227.yu_minigroup.dto;

import java.util.Map;

public class GroupItem {
    private boolean isAd, isAdmin;
    private int memberCount;
    private long timestamp;
    private String id, author, authorUid, image, name, info, description, joinType;
    private Map<String, Boolean> members;

    public GroupItem() {
    }

    public GroupItem(String id, boolean isAd, boolean isAdmin, long timestamp, String author, String authorUid, String image, String name, String info, String description, String joinType) {
        this.id = id;
        this.isAd = isAd;
        this.isAdmin = isAdmin;
        this.timestamp = timestamp;
        this.author = author;
        this.authorUid = authorUid;
        this.image = image;
        this.name = name;
        this.info = info;
        this.description = description;
        this.joinType = joinType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
