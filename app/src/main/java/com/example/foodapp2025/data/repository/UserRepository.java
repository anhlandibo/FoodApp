package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.remote.UserRemoteDataSource;

public class UserRepository {
    private final UserRemoteDataSource userRemoteDataSource;

    public UserRepository() {
        userRemoteDataSource = new UserRemoteDataSource();
    }

    // Lấy thông tin tọa độ người dùng
    public LiveData<UserModel> getUserLocation(String userId) {
        return userRemoteDataSource.getUserLocation(userId);
    }

    // Cập nhật tọa độ người dùng
    public void updateUserLocation(String userId, double lat, double lon) {
        userRemoteDataSource.updateUserLocation(userId, lat, lon);
    }

    // Lấy userID của người dùng hiện tại
    public String getUserID(){
        return userRemoteDataSource.getUserID();
    }
    public LiveData<UserModel> getUserInformation(String userId) {
        return userRemoteDataSource.getUserInformation(userId);
    }
}
