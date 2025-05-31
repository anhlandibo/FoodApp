package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.data.remote.VoucherRemoteDataSource;
import com.google.firebase.firestore.FirebaseFirestore;

public class VoucherRepository {
    private final VoucherRemoteDataSource voucherRemoteDataSource;

    public VoucherRepository() {
        // Create Firestore instance and point to your vouchers collection
        this.voucherRemoteDataSource = new VoucherRemoteDataSource(
                FirebaseFirestore.getInstance().collection("vouchers")
        );
    }

    public LiveData<VoucherModel> getVoucherByCode(String code){
        return voucherRemoteDataSource.getVoucherByCode(code);
    }
}
