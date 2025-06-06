package com.example.foodapp2025.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.firebase.Timestamp;

public class OrderModel implements Serializable {

    //Constants for Firebase Firestore
    //Keys for an item's value
    //Vouchers are a mystery for now, since it stores another array, not a string
//    private static final String DELIVERY_FEE = "deliveryFee";
//    private static final String IMAGE_URL = "imageUrl";
//    private static final String NAME = "name";
//    private static final String PRICE = "price";
//    private static final String QUANTITY = "quantity";
//    private static final String SUBTOTAL = "subtotal";
//    private static final String TAX_AMOUNT = "taxAmount";
//    private static final String TOTAL = "total";
//    private static final String VOUCHER_CODE = "voucherCode";
    private String id;
    private ArrayList<Map<String, Object>> items;
    private int subtotal;
    private int tax;
    private long timestamp;
    private int total;
    private String userId;
    private String status;
    private String paymentStatus;
    private String paymentMethod;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    //not on database yet
//    private Status status;
    public OrderModel() {
        items = new ArrayList<>();
    }

    public OrderModel(String id, ArrayList<Map<String, Object>> items, int subtotal, int tax, long timestamp, int total, String userId, String paymentStatus, String paymentMethod) {
        //initializing items as an empty ArrayList
        items = new ArrayList<>();
        this.id = id;
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.timestamp = timestamp;
        this.total = total;
        this.userId = userId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    // Getter and Setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for items
    public ArrayList<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Map<String, Object>> items) {
        this.items = items;
    }

    // Getter and Setter for subtotal
    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    // Getter and Setter for tax
    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    // Getter and Setter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter and Setter for total
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getOrderedDate() {
        return new Date(timestamp);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }
}
