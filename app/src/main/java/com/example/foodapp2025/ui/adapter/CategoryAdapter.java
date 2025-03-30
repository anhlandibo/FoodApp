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
import com.example.foodapp2025.data.model.CategoryModel;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private ArrayList<CategoryModel> categoryList;

    public CategoryAdapter(ArrayList<CategoryModel> categoryList){
        this.categoryList = categoryList;
    }

    public void setCategoryList(ArrayList<CategoryModel> newList){
        this.categoryList = newList;
        notifyDataSetChanged(); //cap nhat du lieu
    }
    @NonNull
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        CategoryModel categoryModel = categoryList.get(position);
        holder.categoryName.setText(categoryModel.getName());
        //Load anh tu Firestore vao Glide
        Glide.with(holder.itemView.getContext())
                .load(categoryModel.getImageUrl())
                .into(holder.categoryImage);
        //co the them anh tam va anh loi

        holder.itemView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = new Bundle();
            bundle.putString("categoryName", categoryModel.getName());
            navController.navigate(R.id.categoryDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
