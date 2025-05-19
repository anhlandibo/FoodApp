package com.example.foodapp2025.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CartModel;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>{
    private List<CartModel> orderDetails;

    public OrderDetailAdapter() {
        orderDetails = new ArrayList<>();
    }
    public OrderDetailAdapter(List<CartModel> orderDetails) {
        this.orderDetails = orderDetails;
    }
    public void setOrderDetails(List<CartModel> orderDetails) {
        this.orderDetails = orderDetails;
        notifyDataSetChanged();
    }
    public void addItem(CartModel cartModel) {
        orderDetails.add(cartModel);
        notifyItemInserted(orderDetails.size() - 1);
    }

    @NonNull
    @Override
    public OrderDetailAdapter.OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_item, parent, false);
        return new OrderDetailAdapter.OrderDetailViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return orderDetails != null ? orderDetails.size() : 0;
    }
    public void onBindViewHolder(@NonNull OrderDetailAdapter.OrderDetailViewHolder holder, int position) {
        CartModel cartModel = orderDetails.get(position);
        holder.orderDetailName.setText(cartModel.getName());
        holder.orderDetailCost.setText(cartModel.getPrice() * cartModel.getQuantity() + " VND");
        holder.orderDetailQuantity.setText(String.valueOf(cartModel.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(cartModel.getImageUrl())
                .into(holder.orderDetailImage);
    }

    public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        ImageView orderDetailImage;
        TextView orderDetailName, orderDetailQuantity, orderDetailCost;


        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDetailImage = itemView.findViewById(R.id.orderDetailImage);
            orderDetailName = itemView.findViewById(R.id.orderDetailName);
            orderDetailQuantity = itemView.findViewById(R.id.orderDetailQuantity);
            orderDetailCost = itemView.findViewById(R.id.orderDetailCost);
        }
    }
}
