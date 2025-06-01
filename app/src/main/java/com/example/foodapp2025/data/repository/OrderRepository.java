package com.example.foodapp2025.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.data.remote.OrderRemoteDataSource;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class OrderRepository {
    private final OrderRemoteDataSource orderRemoteDataSource;
    public OrderRepository(OrderRemoteDataSource orderRemoteDataSource){
        this.orderRemoteDataSource = orderRemoteDataSource;
    }
    public LiveData<ArrayList<OrderModel>> getCurrentUsersOrders(){
        return orderRemoteDataSource.getCurrentUsersOrders();
    }

    public Task<Void> updateOrderStatus(String orderId, String status){
        Log.d("OrderRemoteDataSource", "Attempting to update status for order ID: " + orderId + " to: " + status);
        return orderRemoteDataSource.updateOrderAndPaymentStatus(orderId, status, "paid");
    }
}
