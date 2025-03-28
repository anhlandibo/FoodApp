package com.example.foodapp2025.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.repository.AuthRepository;
import com.example.foodapp2025.utils.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel {
    private final AuthRepository authRepository;

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Result<UserModel>> login(String email, String password) {
        return authRepository.login(email, password);
    }
}