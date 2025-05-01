// CommentModel.java
package com.example.foodapp2025.data.model;

import java.util.HashMap;
import java.util.Map;

public class CommentModel {
    private String userId;
    private String userName;
    private String text;
    private long timestamp;
    private float rating; // 1 - 5 sao

    public CommentModel() {}

    public CommentModel(String userId, String userName, String text, long timestamp, float rating) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("text", text);
        map.put("timestamp", timestamp);
        map.put("rating", rating);
        return map;
    }
}
