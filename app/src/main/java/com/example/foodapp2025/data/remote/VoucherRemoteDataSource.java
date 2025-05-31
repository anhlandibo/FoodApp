package com.example.foodapp2025.data.remote;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.data.model.VoucherModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
public class VoucherRemoteDataSource {
    private final CollectionReference voucherCollection;
    public VoucherRemoteDataSource(CollectionReference voucherCollection) {
        this.voucherCollection = voucherCollection;
    }

    public LiveData<ArrayList<VoucherModel>> getVouchers(String code){
        MutableLiveData<ArrayList<VoucherModel>> listMutableLiveData = new MutableLiveData<>();
        voucherCollection.whereEqualTo("categoryName", code).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                ArrayList<VoucherModel> voucherModelArrayList = new ArrayList<>();

                for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                    VoucherModel voucherModel = queryDocumentSnapshot.toObject(VoucherModel.class);
                    voucherModelArrayList.add(voucherModel);
                }

                listMutableLiveData.setValue(voucherModelArrayList);
            }
            else{
                listMutableLiveData.setValue(new ArrayList<>());
            }

        });
        return listMutableLiveData;
    }

    public LiveData<VoucherModel> getVoucherByCode(String code) {
        MutableLiveData<VoucherModel> resultLiveData = new MutableLiveData<>();

        voucherCollection
                .whereEqualTo("code", code) // exact match
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        VoucherModel voucher = doc.toObject(VoucherModel.class);
                        resultLiveData.setValue(voucher);
                    } else {
                        resultLiveData.setValue(null); // no match
                    }
                })
                .addOnFailureListener(e -> {
                    resultLiveData.setValue(null); // could also use Result wrapper to indicate error
                    Log.e("VoucherCheck", "Error fetching voucher", e);
                });

        return resultLiveData;
    }
}
