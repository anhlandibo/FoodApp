package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.CategoryModel;
import com.example.foodapp2025.data.remote.CategoryRemoteDataSource;
import com.example.foodapp2025.data.repository.CategoryRepository;

import java.util.ArrayList;

public class CategoryViewModel extends ViewModel {
    private final CategoryRepository categoryRepository = new CategoryRepository(new CategoryRemoteDataSource());

    public LiveData<ArrayList<CategoryModel>> getCategories(){
        return categoryRepository.getCategories();
    }
}
