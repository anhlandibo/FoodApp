package com.example.foodapp2025.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.FoodModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FoodRemoteDataSource {
    private final CollectionReference foodCollection;
    public FoodRemoteDataSource(){
        foodCollection = FirebaseService.getInstance().getFirestore().collection("food");
    }
    public LiveData<ArrayList<FoodModel>> getMenuItems(String category){
        MutableLiveData<ArrayList<FoodModel>> listMutableLiveData = new MutableLiveData<>();

        foodCollection.whereEqualTo("categoryName", category).get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && task.getResult() != null){
               ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

               for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                   FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                   foodModelArrayList.add(foodModel);
               }

               listMutableLiveData.setValue(foodModelArrayList);
           }
           else{
               listMutableLiveData.setValue(new ArrayList<>());
           }

        });
        return listMutableLiveData;
    }
    public LiveData<ArrayList<FoodModel>> getPopularFood(){
        MutableLiveData<ArrayList<FoodModel>> listMutableLiveData = new MutableLiveData<>();

        foodCollection.whereEqualTo("isPopular", true).get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && task.getResult() != null) {
               ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

               for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                   FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                   foodModelArrayList.add(foodModel);
               }
               listMutableLiveData.setValue(foodModelArrayList);
           }
           else{
               listMutableLiveData.setValue(new ArrayList<>());
           }
        });
        return listMutableLiveData;
    }
}
