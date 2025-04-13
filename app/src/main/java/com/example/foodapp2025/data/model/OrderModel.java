package com.example.foodapp2025.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class OrderModel implements Serializable {
    private String id;
    private ArrayList<Map<String, String>> items;
    private int subtotal;
    private int tax;
    private long timestamp;
    private int total;
    private String userId;

    public OrderModel() {
        items = new ArrayList<>();
    }

    public OrderModel(String id, ArrayList<Map<String, String>> items, int subtotal, int tax, long timestamp, int total, String userId) {
        //initializing items as an empty ArrayList
        items = new ArrayList<>();
        this.id = id;
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.timestamp = timestamp;
        this.total = total;
        this.userId = userId;
    }


    // Getter and Setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for items
    public ArrayList<Map<String, String>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Map<String, String>> items) {
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

}
