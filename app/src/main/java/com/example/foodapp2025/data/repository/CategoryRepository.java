package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.CategoryModel;
import com.example.foodapp2025.data.remote.CategoryRemoteDataSource;

import java.util.ArrayList;

public class CategoryRepository {
    private final CategoryRemoteDataSource categoryRemoteDataSource;
    public CategoryRepository(CategoryRemoteDataSource categoryRemoteDataSource){
        this.categoryRemoteDataSource = categoryRemoteDataSource;
    }
    public LiveData<ArrayList<CategoryModel>> getCategories(){
        return categoryRemoteDataSource.getCategories();
    }
}
