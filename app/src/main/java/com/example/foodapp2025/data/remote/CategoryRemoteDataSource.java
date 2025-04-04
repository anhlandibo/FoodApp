package com.example.foodapp2025.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.CategoryModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CategoryRemoteDataSource {
    private final CollectionReference categoryCollection;

    public CategoryRemoteDataSource(){
        categoryCollection = FirebaseService.getInstance().getFirestore().collection("categories");
    }
    public LiveData<ArrayList<CategoryModel>> getCategories(){
        MutableLiveData<ArrayList<CategoryModel>> listMutableLiveData = new MutableLiveData<>();

        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                ArrayList<CategoryModel> categoryModels = new ArrayList<>();

                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    CategoryModel categoryModel = queryDocumentSnapshot.toObject(CategoryModel.class);
                    categoryModels.add(categoryModel);
                }
                listMutableLiveData.setValue(categoryModels);
            }
            else{
                listMutableLiveData.setValue(null);
            }
        });
        return listMutableLiveData;
    }
}
