package com.example.mysns;

public class Post {
    private String userId;
    private String userNickname;
    private String postedImageUri;
    private String postedTime;
    private String description;

    public Post(String userId, String userNickname, String postedImageUri, String postedTime, String description) {
        this.userId = userId;
        this.userNickname = userNickname;
        this.postedImageUri = postedImageUri;
        this.postedTime = postedTime;
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getPostedImageUri() {
        return postedImageUri;
    }

    public void setPostedImageUri(String postedImageUri) {
        this.postedImageUri = postedImageUri;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(String postedTime) {
        this.postedTime = postedTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
