package com.example.foodapp2025.data.remote;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.foodapp2025.data.model.FoodModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FoodRemoteDataSource {
    private final CollectionReference foodCollection;

    public FoodRemoteDataSource() {
        foodCollection = FirebaseService.getInstance().getFirestore().collection("food");
    }

    public LiveData<ArrayList<FoodModel>> getMenuItems(String category) {
        MutableLiveData<ArrayList<FoodModel>> listMutableLiveData = new MutableLiveData<>();

        foodCollection.whereEqualTo("categoryName", category).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                    foodModel.setId(queryDocumentSnapshot.getId()); //
                    foodModelArrayList.add(foodModel);
                }

                listMutableLiveData.setValue(foodModelArrayList);
            } else {
                listMutableLiveData.setValue(new ArrayList<>());
                Log.e("FoodRemoteDataSource", "Error getting menu items: ", task.getException());
            }
        });
        return listMutableLiveData;
    }

    public LiveData<ArrayList<FoodModel>> getPopularFood() {
        MutableLiveData<ArrayList<FoodModel>> listMutableLiveData = new MutableLiveData<>();

        foodCollection.whereEqualTo("isPopular", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                    foodModel.setId(queryDocumentSnapshot.getId()); //
                    foodModelArrayList.add(foodModel);
                }
                listMutableLiveData.setValue(foodModelArrayList);
            } else {
                listMutableLiveData.setValue(new ArrayList<>());
                Log.e("FoodRemoteDataSource", "Error getting popular food: ", task.getException());
            }
        });
        return listMutableLiveData;
    }

    public LiveData<ArrayList<FoodModel>> getFoodByKeyword(String keyword) {
        MutableLiveData<ArrayList<FoodModel>> resultLiveData = new MutableLiveData<>();

        foodCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<FoodModel> filteredList = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                FoodModel food = doc.toObject(FoodModel.class);
                if (food != null) {
                    food.setId(doc.getId()); //
                    if (food.getName() != null && food.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        filteredList.add(food);
                    }
                }
            }

            resultLiveData.setValue(filteredList);
        }).addOnFailureListener(e -> {
            resultLiveData.setValue(new ArrayList<>());
            Log.e("RemoteDataSource", "Error getting food by keyword: ", e);
        });

        return resultLiveData;
    }

    public LiveData<FoodModel> getMinPriceFood() {
        MutableLiveData<FoodModel> mutableLiveData = new MutableLiveData<>();

        foodCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            FoodModel minFood = null;

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                FoodModel foodModel = doc.toObject(FoodModel.class);
                if (foodModel != null) {
                    foodModel.setId(doc.getId());

                    if (minFood == null || (foodModel.getPrice() != null && foodModel.getPrice() < minFood.getPrice())) {
                        minFood = foodModel;
                    }
                }
            }
            mutableLiveData.setValue(minFood);
        }).addOnFailureListener(e -> {
            mutableLiveData.setValue(null);
            Log.e("RemoteDataSource", "Error getting min price food: ", e);
        });
        return mutableLiveData;
    }

    public LiveData<FoodModel> getMaxPriceFood() {
        MutableLiveData<FoodModel> mutableLiveData = new MutableLiveData<>();

        foodCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            FoodModel maxFood = null;

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                FoodModel foodModel = doc.toObject(FoodModel.class);
                if (foodModel != null) {
                    foodModel.setId(doc.getId());

                    if (maxFood == null || (foodModel.getPrice() != null && foodModel.getPrice() > maxFood.getPrice())) {
                        maxFood = foodModel;
                    }
                }
            }
            mutableLiveData.setValue(maxFood);
        }).addOnFailureListener(e -> {
            mutableLiveData.setValue(null);
            Log.e("RemoteDataSource", "Error getting max price food: ", e);
        });
        return mutableLiveData;
    }
}