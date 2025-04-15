package com.example.foodapp2025.data.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class CartModel {
    private String imageUrl;
    private String name;
    private int price;
    private int quantity;
    private static final double TAX_RATE = 0.1; // 10 % tax
    private static final Integer DELIVERY_FEE = 20000; // Fixed delivery fee
    private static final ArrayList<String> VOUCHER_CODE = new ArrayList<>(Arrays.asList("FREE2SHIP", "10DEAL"));

    public CartModel(String imageUrl, String name, int price, int quantity) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Calculate subtotal (price * quantity)
    public double getSubtotal() {
        return price * quantity;
    }

    // Calculate tax (subtotal * tax rate)
    public double getTaxAmount() {
        return getSubtotal() * TAX_RATE;
    }

    // Get delivery fee
    public double getDeliveryFee() {
        return DELIVERY_FEE;
    }

    public ArrayList<String> getVoucherCode(){
        return VOUCHER_CODE;
    }

    // Calculate total (subtotal + tax + delivery fee)
    public double getTotal() {
        return getSubtotal() + getTaxAmount() + getDeliveryFee();
    }

}