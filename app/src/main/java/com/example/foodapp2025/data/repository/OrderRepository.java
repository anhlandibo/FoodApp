package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;

import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.data.remote.OrderRemoteDataSource;

import java.util.ArrayList;

public class OrderRepository {
    private final OrderRemoteDataSource orderRemoteDataSource;
    public OrderRepository(OrderRemoteDataSource orderRemoteDataSource){
        this.orderRemoteDataSource = orderRemoteDataSource;
    }
    public LiveData<ArrayList<OrderModel>> getCurrentUsersOrders(){
        return orderRemoteDataSource.getCurrentUsersOrders();
    }
}
