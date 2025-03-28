package com.example.foodapp2025.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.BannerModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class BannerRemoteDataSource {
    private final CollectionReference bannerCollection;
    public BannerRemoteDataSource(){
        bannerCollection = FirebaseService.getInstance().getFirestore().collection("banners");
    }

    //multithread programming
    public LiveData<ArrayList<BannerModel>> getBanners(){
        MutableLiveData<ArrayList<BannerModel>> listMutableLiveData = new MutableLiveData<>();

        bannerCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                ArrayList<BannerModel> banners = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                    BannerModel bannerModel = queryDocumentSnapshot.toObject(BannerModel.class);
                    banners.add(bannerModel); //always != null
                }
                listMutableLiveData.setValue(banners);
            }
            else{
                listMutableLiveData.setValue(null);
            }
        });
        return listMutableLiveData;
    }
}
