package com.example.foodapp2025.data.model;

import java.util.HashMap;
import java.util.Map;

public class CommentModel {
    private String userId;
    private String userName;
    private String text;
    private long timestamp;
    private float rating; // 1 - 5 sao
    private String moderationStatus; // <-- THÊM TRƯỜNG NÀY

    public CommentModel() {}

    public CommentModel(String userId, String userName, String text, long timestamp, float rating, String moderationStatus) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
        this.rating = rating;
        this.moderationStatus = moderationStatus; // <-- CẬP NHẬT CONSTRUCTOR CÓ ĐẦY ĐỦ THAM SỐ
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

    // <-- THÊM GETTER VÀ SETTER CHO moderationStatus
    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("text", text);
        map.put("timestamp", timestamp);
        map.put("rating", rating);
        map.put("moderationStatus", moderationStatus); // <-- THÊM TRƯỜNG VÀO MAP
        return map;
    }
}