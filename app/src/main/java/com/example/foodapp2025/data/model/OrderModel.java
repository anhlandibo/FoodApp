package com.example.foodapp2025.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.firebase.Timestamp;

public class OrderModel implements Serializable {

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
    private int discountAmount;

    private Map<String, Object> appliedVoucherDetails; // New field for voucher details

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderModel() {
        items = new ArrayList<>();
    }

    public OrderModel(String id, ArrayList<Map<String, Object>> items, int subtotal, int tax, long timestamp, int total, String userId, String paymentStatus, String paymentMethod, int discountAmount) {
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
        this.discountAmount = discountAmount;
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

    // Getter and Setter for appliedVoucherDetails
    public Map<String, Object> getAppliedVoucherDetails() {
        return appliedVoucherDetails;
    }

    public void setAppliedVoucherDetails(Map<String, Object> appliedVoucherDetails) {
        this.appliedVoucherDetails = appliedVoucherDetails;
    }

    // Helper methods to access voucher details easily
    public String getVoucherCode() {
        if (appliedVoucherDetails != null) {
            return (String) appliedVoucherDetails.get("code");
        }
        return null;
    }

    public String getVoucherType() {
        if (appliedVoucherDetails != null) {
            return (String) appliedVoucherDetails.get("type");
        }
        return null;
    }

    public Object getVoucherValue() {
        if (appliedVoucherDetails != null) {
            return appliedVoucherDetails.get("value");
        }
        return null;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }
}