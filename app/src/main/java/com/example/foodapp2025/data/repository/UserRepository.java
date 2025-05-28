package com.example.foodapp2025.data.repository;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.remote.UserRemoteDataSource;
import com.example.foodapp2025.databinding.FragmentProfileBinding;

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
    public void updateUserLocation(Context context, String userId, double lat, double lon) {
        userRemoteDataSource.updateUserLocation(context, userId, lat, lon);
    }

    // Lấy userID của người dùng hiện tại
    public String getUserID(){
        return userRemoteDataSource.getUserID();
    }
    public LiveData<UserModel> getUserInformation(String userId) {
        return userRemoteDataSource.getUserInformation(userId);
    }

    public void handleEditBtn(View view, FragmentProfileBinding binding, boolean isEditBtnPressed) {
        userRemoteDataSource.handleEditBtn(view, binding, isEditBtnPressed);
    }

    public void handleResetPassword(Context context) {
        userRemoteDataSource.handleResetPassword(context);
    }
}
