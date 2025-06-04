package com.example.foodapp2025.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.List;

public class FavouriteFoodAdapter extends RecyclerView.Adapter<FavouriteFoodAdapter.FavouriteFoodViewHolder> {
    private Context context;
    private List<FoodModel> foodList;

    public FavouriteFoodAdapter(Context context, List<FoodModel> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<FoodModel> newData) {
        this.foodList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavouriteFoodAdapter.FavouriteFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FavouriteFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteFoodAdapter.FavouriteFoodViewHolder holder, int position) {
        FoodModel foodModel = foodList.get(position);
        if (foodModel == null) return;
        holder.foodName.setText(foodModel.getName());
        holder.foodPrice.setText(foodModel.getPrice() + " $");
        holder.foodStart.setText(String.valueOf(foodModel.getStar()));
        holder.foodTime.setText(foodModel.getTime());
        Glide.with(holder.itemView.getContext())
                .load(foodModel.getImageUrl())
                .into(holder.foodImage);


        holder.itemView.setOnClickListener(v -> {
            FoodModel selectedFood = foodList.get(position);
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = new Bundle();
            bundle.putString("foodName", foodModel.getName());
            bundle.putSerializable("food", selectedFood);
            navController.navigate(R.id.foodDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FavouriteFoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice, foodTime, foodStart;
        public FavouriteFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodTime = itemView.findViewById(R.id.popularFoodTime);
            foodStart = itemView.findViewById(R.id.ratingTxt);        }
    }
}
