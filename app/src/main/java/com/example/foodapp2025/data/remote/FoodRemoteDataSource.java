package com.example.foodapp2025.data.remote;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.foodapp2025.data.model.FoodModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query; // Import Query để sử dụng whereEqualTo
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FoodRemoteDataSource {
    private final CollectionReference foodCollection;

    public FoodRemoteDataSource() {
        foodCollection = FirebaseService.getInstance().getFirestore().collection("food");
    }

    // Helper method to get a base query with isDeleted = false
    private Query getBaseFoodQuery() {
        return foodCollection.whereEqualTo("isDeleted", false);
    }

    public LiveData<ArrayList<FoodModel>> getMenuItems(String category) {
        MutableLiveData<ArrayList<FoodModel>> listMutableLiveData = new MutableLiveData<>();

        getBaseFoodQuery() // Sử dụng base query
                .whereEqualTo("categoryName", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                            foodModel.setId(queryDocumentSnapshot.getId());
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

        getBaseFoodQuery() // Sử dụng base query
                .whereEqualTo("isPopular", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<FoodModel> foodModelArrayList = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FoodModel foodModel = queryDocumentSnapshot.toObject(FoodModel.class);
                            foodModel.setId(queryDocumentSnapshot.getId());
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

        getBaseFoodQuery() // Sử dụng base query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<FoodModel> filteredList = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodModel food = doc.toObject(FoodModel.class);
                        if (food != null) {
                            food.setId(doc.getId());
                            // Bạn cần đảm bảo trường 'name' trong Firestore của bạn khớp với getter 'getName()'
                            // và dữ liệu của nó không null.
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

        getBaseFoodQuery() // Sử dụng base query
                .orderBy("price", Query.Direction.ASCENDING) // Sắp xếp theo giá tăng dần
                .limit(1) // Chỉ lấy 1 món có giá thấp nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    FoodModel minFood = null;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        minFood = doc.toObject(FoodModel.class);
                        if (minFood != null) {
                            minFood.setId(doc.getId());
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

        getBaseFoodQuery() // Sử dụng base query
                .orderBy("price", Query.Direction.DESCENDING) // Sắp xếp theo giá giảm dần
                .limit(1) // Chỉ lấy 1 món có giá cao nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    FoodModel maxFood = null;
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        maxFood = doc.toObject(FoodModel.class);
                        if (maxFood != null) {
                            maxFood.setId(doc.getId());
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