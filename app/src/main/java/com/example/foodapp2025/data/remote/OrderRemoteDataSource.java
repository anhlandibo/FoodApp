package com.example.foodapp2025.data.remote;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.data.model.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRemoteDataSource {
    private final CollectionReference orderCollection;

    public OrderRemoteDataSource() {
        orderCollection = FirebaseService.getInstance().getFirestore().collection("orders");
    }


    public LiveData<ArrayList<OrderModel>> getCurrentUsersOrders() {
        MutableLiveData<ArrayList<OrderModel>> listMutableLiveData = new MutableLiveData<>();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        orderCollection.whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<OrderModel> orderModelArrayList = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            OrderModel orderModel = queryDocumentSnapshot.toObject(OrderModel.class);
                            orderModel.setId(queryDocumentSnapshot.getId());
                            orderModelArrayList.add(orderModel);
                        }
                        listMutableLiveData.setValue(orderModelArrayList);
                    } else {
                        listMutableLiveData.setValue(new ArrayList<>());

                        Log.e("FirestoreError", "Error getting documents: ", task.getException());

                    }
                });
        return listMutableLiveData;
    }

    //
    public Task<Void> updateOrderStatus(String orderId, String status){
        Log.d("OrderRemoteDataSource", "Attempting to update status for order ID: " + orderId + " to: " + status);
        return orderCollection.document(orderId)
                .update("status", status);
    }

    public Task<Void> updateOrderAndPaymentStatus(String orderId, String status, String paymentStatus){
        Log.d("OrderRemoteDataSource", "Attempting to update order ID: " + orderId + " to orderStatus: " + status + " and paymentStatus: " + paymentStatus);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("paymentStatus", paymentStatus);
        return orderCollection.document(orderId)
                .update(updates);
    }


    public Task<Void> reportOrder(OrderModel orderModel) {
        DocumentReference orderRef = orderCollection.document(orderModel.getId());

        // Update existing reportStatus and add new reportAdditionalInfo field
        return orderRef.update(
                        "reportStatus", orderModel.getReportStatus(),
                        "reportAdditionalInfo", orderModel.getReportAdditionalInfo()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrderReport", "Report submitted successfully");
                    // Show success message
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderReport", "Error submitting report", e);
                    // Show error message
                });
    }

}