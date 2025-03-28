package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.data.remote.BannerRemoteDataSource;
import com.example.foodapp2025.data.remote.FirebaseService;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BannerRepository{

    private final BannerRemoteDataSource bannerRemoteDataSource;
    public BannerRepository(BannerRemoteDataSource bannerRemoteDataSource){
        this.bannerRemoteDataSource = bannerRemoteDataSource;
    }

    public LiveData<ArrayList<BannerModel>> getBanners(){
        return bannerRemoteDataSource.getBanners();
    }
}
