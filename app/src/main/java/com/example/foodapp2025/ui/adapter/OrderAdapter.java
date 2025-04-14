package com.example.foodapp2025.ui.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.orderId.setText(orderModel.getId());
        holder.orderTime.setText(String.valueOf(orderModel.getTimestamp()));
        holder.orderStatus.setText(String.valueOf(orderModel.getStatus()));


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

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);
        }
    }
}
