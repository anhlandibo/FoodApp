package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.data.remote.FoodRemoteDataSource;

import java.util.ArrayList;

public class FoodRepository {
    private final FoodRemoteDataSource foodRemoteDataSource;

    public FoodRepository(FoodRemoteDataSource foodRemoteDataSource){
        this.foodRemoteDataSource = foodRemoteDataSource;
    }

    public LiveData<ArrayList<FoodModel>> getMenuItems(String category){
        return foodRemoteDataSource.getMenuItems(category);
    }

    public LiveData<ArrayList<FoodModel>> getPopularFood(){
        return foodRemoteDataSource.getPopularFood();
    }
}
