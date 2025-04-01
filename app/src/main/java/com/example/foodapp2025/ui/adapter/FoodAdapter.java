package com.example.foodapp2025.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private ArrayList<FoodModel> foodModels = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setFoodList(ArrayList<FoodModel> foodModels) {
        this.foodModels = foodModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodAdapter.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FoodAdapter.FoodViewHolder holder, int position) {
        FoodModel foodModel = foodModels.get(position);
        holder.foodName.setText(foodModel.getName());
        holder.foodPrice.setText(foodModel.getPrice() + " VND");
        holder.foodStart.setText(String.valueOf(foodModel.getStar()));
        holder.foodTime.setText(foodModel.getTime());

        Glide.with(holder.itemView.getContext())
                .load(foodModel.getImageUrl())
                .into(holder.foodImage);
    }

    @Override
    public int getItemCount() {
        return foodModels.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice, foodTime, foodStart;


        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.foodImage);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodTime = itemView.findViewById(R.id.timeTxt);
            foodStart = itemView.findViewById(R.id.ratingTxt);
        }
    }
}
