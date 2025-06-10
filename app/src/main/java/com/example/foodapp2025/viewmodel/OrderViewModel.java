package com.example.foodapp2025.viewmodel;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.data.remote.OrderRemoteDataSource;
import com.example.foodapp2025.data.repository.OrderRepository;

import java.util.ArrayList;

public class OrderViewModel extends ViewModel {
    private final OrderRepository orderRepository = new OrderRepository(new OrderRemoteDataSource());

    public LiveData<ArrayList<OrderModel>> getCurrentUsersOrders() {
        return orderRepository.getCurrentUsersOrders();
    }

    public void confirmOrderReceived(String orderId) {
        Log.d("OrderViewModel", "Confirming order received for ID: " + orderId);
        // Gọi phương thức update trong Repository
        orderRepository.updateOrderStatus(orderId, "delivered") // Truyền trạng thái mới
                .addOnCompleteListener(task -> { // Lắng nghe kết quả từ Task
                    if (task.isSuccessful()) {
                        Log.d("OrderViewModel", "Order status updated successfully via ViewModel.");
                    } else {
                        Log.e("OrderViewModel", "Error updating order status via ViewModel.", task.getException());
                    }
                });
    }

    public void reportOrder(OrderModel orderModel, View itemView) {
        orderRepository.reportOrder(orderModel)
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrderReport", "Report submitted successfully");
                    // Update UI - hide report link, show "reported" text
                    TextView reportLink = itemView.findViewById(R.id.btn_report_order);
                    TextView reportedText = itemView.findViewById(R.id.txt_order_reported);

                    reportLink.setVisibility(View.GONE);
                    reportedText.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderReport", "Error submitting report", e);
                });
    }
}
