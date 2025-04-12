package com.example.foodapp2025.data.remote;

import android.media.MediaPlayer;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.auth.User;

public class UserRemoteDataSource {
    private final CollectionReference userRef;

    public UserRemoteDataSource() {
        userRef = FirebaseService.getInstance().getFirestore().collection("users");
    }

    // Lấy thông tin tọa độ người dùng từ Firestore
    public LiveData<UserModel> getUserLocation(String userId) {
        MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
        userRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserModel user = documentSnapshot.toObject(UserModel.class);
                userLiveData.setValue(user);
            } else
                // Handle khi không tìm thấy người dùng
                userLiveData.setValue(null);
        }).addOnFailureListener(e -> {
            userLiveData.setValue(null);
        });
        return userLiveData;
    }

    // Cập nhật thông tin tọa độ người dùng
    public void updateUserLocation(String uid, double lat, double lon) {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        userRef.document(uid).update("location", geoPoint)
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {

                });
    }

    // Lấy userId của người dùng hiện tại
    public String getUserID(){
        return FirebaseService.getInstance().getFirebaseAuth().getCurrentUser().getUid();
    }

    // Lấy thông tin của user
    public LiveData<UserModel> getUserInformation(String userId) {
        MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
        userRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserModel user = documentSnapshot.toObject(UserModel.class);
                userLiveData.setValue(user);
            } else
                // Handle khi không tìm thấy người dùng
                userLiveData.setValue(null);
        }).addOnFailureListener(e -> {
            userLiveData.setValue(null);
        });
        return userLiveData;

    }
}