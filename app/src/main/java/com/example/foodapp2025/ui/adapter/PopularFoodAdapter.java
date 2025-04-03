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

public class PopularFoodAdapter extends RecyclerView.Adapter<PopularFoodAdapter.PopularFoodViewHolder> {
    private ArrayList<FoodModel> popularFoodList = new ArrayList<>();
    public PopularFoodAdapter(){}
    public PopularFoodAdapter(ArrayList<FoodModel> popularFoodList){
        this.popularFoodList = popularFoodList;
    }


    @NonNull
    @Override
    public PopularFoodAdapter.PopularFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_food, parent, false);
        return new PopularFoodViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PopularFoodAdapter.PopularFoodViewHolder holder, int position) {
        FoodModel popularFood = popularFoodList.get(position);

        holder.popularFoodName.setText(popularFood.getName());
        holder.popularFoodPrice.setText(popularFood.getPrice() + " VND");
        holder.popularFoodTime.setText(popularFood.getTime());
        holder.popularFoodStar.setText(popularFood.getStar());

        Glide.with(holder.itemView.getContext())
                .load(popularFood.getImageUrl())
                .into(holder.popularFoodImage);
    }

    @Override
    public int getItemCount() {
        return popularFoodList.size();
    }

    public static class PopularFoodViewHolder extends RecyclerView.ViewHolder {
        ImageView popularFoodImage;
        TextView popularFoodName, popularFoodPrice, popularFoodStar, popularFoodTime;
        public PopularFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            popularFoodImage = itemView.findViewById(R.id.popularFoodImage);
            popularFoodName = itemView.findViewById(R.id.popularFoodName);
            popularFoodPrice = itemView.findViewById(R.id.popularFoodPrice);
            popularFoodTime = itemView.findViewById(R.id.popularFoodTime);
            popularFoodStar = itemView.findViewById(R.id.popularFoodStar);
        }
    }
}
