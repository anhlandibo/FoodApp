package com.example.foodapp2025.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.repository.UserRepository;
import com.example.foodapp2025.databinding.FragmentProfileBinding;

public class UserViewModel extends ViewModel {
    private UserRepository userRepository;
    private LiveData<UserModel> userModelLiveData;

    public UserViewModel() {
        userRepository = new UserRepository();
    }

    // Lấy thông tin người dùng
    public LiveData<UserModel> getUserLocation(String userId) {
        if (userModelLiveData == null) {
            userModelLiveData = userRepository.getUserLocation(userId);
        }
        return userModelLiveData;
    }

    // Cập nhật tọa độ người dùng
    public void updateUserLocation(Context context, String userId, double latitude, double longitude) {
        userRepository.updateUserLocation(context, userId, latitude, longitude);
    }

    // Lấy userID của người dùng hiện tại
    public String getUserID(){
        return userRepository.getUserID();
    }
    public LiveData<UserModel> getUserInformation(String userId) {
        return userRepository.getUserInformation(userId);
    }

    public void handleEditBtn(View view, FragmentProfileBinding binding, boolean isEditBtnPressed) {
        userRepository.handleEditBtn(view, binding, isEditBtnPressed);
    }

    public void handleResetPassword(Context context) {
        userRepository.handleResetPassword(context);
    }

    public void uploadAvatar(Uri photoUri, Context context) {
        userRepository.uploadAvatar(photoUri, context);
    }

    public LiveData<String> getAvatarUploadStatus() {
        return userRepository.getAvatarUploadStatus();
    }
}
