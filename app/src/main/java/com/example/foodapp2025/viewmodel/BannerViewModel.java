package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.data.remote.BannerRemoteDataSource;
import com.example.foodapp2025.data.repository.BannerRepository;

import java.util.ArrayList;

public class BannerViewModel extends ViewModel {
    private final BannerRepository bannerRepository = new BannerRepository(new BannerRemoteDataSource());

    public LiveData<ArrayList<BannerModel>> loadBanner(){
        return bannerRepository.getBanners();
    }
}
