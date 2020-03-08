package com.example.mysns;

public class Post {
    private String userId;
    private String postedImageUri;
    private String postedTime;
    private String description;
    private int numLike;
    private long createdAt;

    public Post(){}

    public Post(String userId, String postedImageUri, String postedTime, String description, long createdAt) {
        this.userId = userId;
        this.postedImageUri = postedImageUri;
        this.postedTime = postedTime;
        this.description = description;
        this.numLike = 0;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public int getNumLike() {
        return numLike;
    }

    public void setNumLike(int numLike) {
        this.numLike = numLike;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
