package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.remote.AuthRemoteDataSource;
import com.example.foodapp2025.utils.Result;
import com.google.firebase.firestore.auth.User;

public class AuthRepository {
    private final AuthRemoteDataSource authRemoteDataSource;
    public AuthRepository(AuthRemoteDataSource authRemoteDataSource){
        this.authRemoteDataSource = authRemoteDataSource;
    }

    public LiveData<Result<UserModel>> login(String email, String password) {
        return authRemoteDataSource.login(email, password);
    }

    public LiveData<Result<UserModel>> register(String email, String password, String name){
        return authRemoteDataSource.register(email, password, name);
    }
}
