package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.repository.AuthRepository;
import com.example.foodapp2025.utils.Result;

public class AuthViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Result<UserModel>> login(String email, String password) {
        return authRepository.login(email, password);
    }
    public LiveData<Result<UserModel>> register(String email, String password, String name){
        return authRepository.register(email, password, name);
    }
}
