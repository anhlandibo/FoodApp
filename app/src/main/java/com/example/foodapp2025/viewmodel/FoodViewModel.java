package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.data.remote.FoodRemoteDataSource;
import com.example.foodapp2025.data.repository.FoodRepository;

import java.util.ArrayList;

public class FoodViewModel extends ViewModel {
    private final FoodRepository foodRepository = new FoodRepository(new FoodRemoteDataSource());

    public LiveData<ArrayList<FoodModel>> getMenuItems(String categoryName) {
        return foodRepository.getMenuItems(categoryName);
    }

    public LiveData<ArrayList<FoodModel>> getPopularFood() {
        return foodRepository.getPopularFood();
    }

    public LiveData<ArrayList<FoodModel>> getFoodByKeyword(String keyword) {
        return foodRepository.getFoodByKeyword(keyword);
    }

    public LiveData<FoodModel> getMinPriceFood() {
        return foodRepository.getMinPriceFood();
    }
    public LiveData<FoodModel> getMaxPriceFood(){
        return foodRepository.getMaxPriceFood();
    }

    public void filterFood(String selectedCategory, int minPrice, boolean isPopularOnly) {

    }
}
