package com.example.foodapp2025.data.model;

import com.google.firebase.firestore.GeoPoint;

public class UserModel {
    private String email;
    private String name;
    private String uid;
    private String phoneNumber;
    private String address;
    private String gender;
    private String dateOfBirth;
    private String photoUrl;
    private GeoPoint location;

    public UserModel() {}

    public UserModel(String email, String name, String uid) {
        this.email = email;
        this.name = name;
        this.uid = uid;
    }

    public UserModel(String email, String name, String uid, String phoneNumber, String address, String gender, String dateOfBirth, String photoUrl, GeoPoint location) {
        this.email = email;
        this.name = name;
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.photoUrl = photoUrl;
        this.location = location;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void getPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
