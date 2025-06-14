package com.example.foodapp2025.data.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class FoodModel implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private Double price;
    private String categoryName;
    private String description;
    private String time;

    private Double star;

    private boolean isPopular;
    private boolean isDeleted;

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @PropertyName("isPopular")

    public boolean getIsPopular() {
        return isPopular;
    }
    @PropertyName("isPopular")
    public void setPopular(boolean popular) {
        isPopular = popular;
    }

    public FoodModel() {}

    public Double getStar() {
        return star;
    }

    public void setStar(Double star) {
        this.star = star;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public FoodModel(String name, String id, String imageUrl, Double price, String categoryName, String description, String time, Double star, boolean isPopular) {
        this.name = name;
        this.id = id;
        this.imageUrl = imageUrl;
        this.price = price;
        this.categoryName = categoryName;
        this.description = description;
        this.time = time;
        this.star = star;
        this.isPopular = isPopular;
    }
}
