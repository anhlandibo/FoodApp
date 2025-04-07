package com.example.foodapp2025.data.model;

import com.google.firebase.firestore.GeoPoint;

public class UserModel {
    private String email;
    private String name;
    private String uid;
    private GeoPoint location;

    public UserModel() {}

    public UserModel(String email, String name, String uid) {
        this.email = email;
        this.name = name;
        this.uid = uid;
    }

    public GeoPoint getLocation(){
        return location;
    }
    public void setLocation(GeoPoint location){
        this.location = location;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
