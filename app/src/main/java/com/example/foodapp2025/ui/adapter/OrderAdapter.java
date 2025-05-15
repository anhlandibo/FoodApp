package com.example.foodapp2025.ui.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.data.model.OrderModel;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<OrderModel> orderModels = new ArrayList<>();
    public OrderAdapter(){}
    public OrderAdapter(ArrayList<OrderModel> orderModels){
        this.orderModels = orderModels;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setOrderList(ArrayList<OrderModel> orderModels) {
        this.orderModels = orderModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderAdapter.OrderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        OrderModel orderModel = orderModels.get(position);
        holder.orderId.setText("Mã đơn: " + orderModel.getId());
        holder.orderTime.setText("Thời gian: " + String.valueOf(orderModel.getTimestamp()));
        holder.orderStatus.setText(String.valueOf(orderModel.getStatus()));

        Button btnConfirm = holder.buttonConfirmReceived;
        if ("completed".equals(orderModel.getStatus())){
            Log.d("OrderAdapter", "Status is 'completed'. Checking if button is null for ID: " + orderModel.getId() + " -> " + (btnConfirm == null)); // Kiểm tra lại null lần cuối trước khi dùng
            if (btnConfirm != null){
                Log.d("OrderAdapter", "Status is 'completed'. About to set button VISIBLE for ID: " + orderModel.getId()); // <<< Log NGAY TRƯỚC setVisibility
                btnConfirm.setVisibility(View.VISIBLE);
                Log.d("OrderAdapter", "Called setVisibility(VISIBLE) for ID: " + orderModel.getId()); // <<< Log NGAY SAU setVisibility
                btnConfirm.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onConfirmReceivedClick(orderModel.getId());
                    }
                });
            }
            else {
                Log.e("OrderAdapter", "Confirm button is NULL despite successful find in ViewHolder for ID: " + orderModel.getId());
            }
        }
        else{
            if (btnConfirm != null) {
                btnConfirm.setVisibility(View.GONE);
                btnConfirm.setOnClickListener(null);
            }
        }


//        holder.itemView.setOnClickListener(v -> {
//            FoodModel selectedFood = foodModels.get(position);
//            NavController navController = Navigation.findNavController(v);
//            Bundle bundle = new Bundle();
//            bundle.putString("foodName", foodModel.getName());
//            bundle.putSerializable("food", selectedFood);
//            navController.navigate(R.id.foodDetailFragment, bundle);
//        });
    }

    @Override
    public int getItemCount() {
        return orderModels.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderTime;
        Button buttonConfirmReceived;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);
            buttonConfirmReceived = itemView.findViewById(R.id.btn_confirm_order);
        }
    }

    public interface OnOrderActionListener {
        void onConfirmReceivedClick(String orderId);
    }
    private OnOrderActionListener listener;

    public void setOnOrderActionListener(OnOrderActionListener listener){
        this.listener = listener;
    }
}
