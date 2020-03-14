package com.example.mysns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private String userId;
    private String postId;
    private String postedImageUri;
    private String postedTime;
    private String description;
    private List<String> likeList;
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
        this.likeList = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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
        numLike = likeList.size();
        return numLike;
    }

    public List<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(List<String> likeList) {
        this.likeList = likeList;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
