package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.data.remote.OrderRemoteDataSource;
import com.example.foodapp2025.data.repository.OrderRepository;

import java.util.ArrayList;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = new OrderRepository(new OrderRemoteDataSource());
    public LiveData<ArrayList<OrderModel>> getCurrentUsersOrders(){
        return orderRepository.getCurrentUsersOrders();
    }
}
