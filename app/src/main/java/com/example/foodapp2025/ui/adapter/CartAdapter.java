package com.example.foodapp2025.ui.adapter;

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
import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.ViewholderCartBinding;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
// Should be order detail from the start
    private List<CartModel> cartList;
    private OnQuantityChangeListener quantityChangeListener;
    private OnDeleteClickListener deleteClickListener;

    public  CartAdapter() {}
    public CartAdapter(List<CartModel> cartList,
                       OnQuantityChangeListener quantityChangeListener,
                       OnDeleteClickListener deleteClickListener) {
        this.cartList = cartList;
        this.quantityChangeListener = quantityChangeListener;
        this.deleteClickListener = deleteClickListener; //  Now it's properly passed in
    }

    public void setCartList(List<CartModel> newCartList) {
        this.cartList = newCartList;
        notifyDataSetChanged();
    }


    public void removeItem(int position) {
        if (position >= 0 && position < cartList.size()) {
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartList.get(position));
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }


    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderCartBinding binding;

        public CartViewHolder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartModel cartItem) {
            binding.productNameTextView.setText(cartItem.getName());
            binding.priceTextView.setText(cartItem.getPrice() + " $");
            binding.quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
            binding.itemTotalTextView.setText((cartItem.getPrice() * cartItem.getQuantity()) + " $");

            Glide.with(binding.getRoot().getContext())
                    .load(cartItem.getImageUrl())
                    .into(binding.cartView);

            binding.increaseButton.setOnClickListener(v -> {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                quantityChangeListener.onQuantityChanged(cartItem);
                notifyItemChanged(getAdapterPosition());
            });

            binding.decreaseButton.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    quantityChangeListener.onQuantityChanged(cartItem);
                    notifyItemChanged(getAdapterPosition());
                }
            });

            binding.deleteBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteClickListener.onDelete(cartItem, position);
                }
            });
        }
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(CartModel cartItem);
    }

    public interface OnDeleteClickListener {
        void onDelete(CartModel cartItem, int position);
    }
}
