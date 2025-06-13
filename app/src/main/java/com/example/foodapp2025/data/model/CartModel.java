package com.example.foodapp2025.data.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class CartModel {
    private String imageUrl;
    private String name;
    private Double price;
    private Long quantity;
    private String note;
    private static final double TAX_RATE = 0.1; // 10 % tax
    private static final Integer DELIVERY_FEE = 5; // Fixed delivery fee
    private static final ArrayList<String> VOUCHER_CODE = new ArrayList<>(Arrays.asList("FREE2SHIP", "10DEAL"));

    public CartModel(){}
    public CartModel(String imageUrl, String name, Double price, Long quantity) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    public CartModel(String imageUrl, String name, Double price, Long quantity, String note) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
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

    // get note for each cart item
    public String getNote() { return note; }

}