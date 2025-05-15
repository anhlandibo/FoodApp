package com.example.foodapp2025.data.model;

import android.annotation.SuppressLint;

import androidx.test.services.events.TimeStamp;

import com.google.firebase.Timestamp;

public class MessageChatModel {
    private String text;
    private int viewType;
    private Timestamp time;

    public MessageChatModel() {}

    public MessageChatModel(String text, int viewType, Timestamp time) {
        this.text = text;
        this.viewType = viewType;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public int getViewType() {
        return viewType;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }
}
