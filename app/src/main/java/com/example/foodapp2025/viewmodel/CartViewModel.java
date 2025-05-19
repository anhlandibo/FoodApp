package com.example.foodapp2025.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.CartModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartViewModel extends ViewModel {

    private static final double TAX_RATE = 0.05;
    private static final double DELIVERY_FEE = 20000.0;

    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> tax = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> orderPlaced = new MutableLiveData<>(false);
    private final MutableLiveData<String> appliedVoucher = new MutableLiveData<>(null);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

    private final Map<String, Double> voucherCodes = new HashMap<String, Double>() {{
        put("SAVE10", 0.10);
        put("DISCOUNT20", 0.20);
        put("WELCOME5", 0.05);
    }};

    public LiveData<List<CartModel>> getCartItems() {
        return cartItems;
    }

    public LiveData<Double> getSubtotal() {
        return subtotal;
    }

    public LiveData<Double> getTax() {
        return tax;
    }

    public LiveData<Double> getTotal() {
        return total;
    }

    public LiveData<String> getAppliedVoucher() {
        return appliedVoucher;
    }

    public LiveData<Boolean> getOrderPlaced() {
        return orderPlaced;
    }

    public CartViewModel() {
        loadCartFromFirestore();
    }

    public void addItem(CartModel item) {
        if (item.getQuantity() <= 0 || userId == null) return;

        List<CartModel> updatedCart = new ArrayList<>(cartItems.getValue());
        boolean found = false;

        // If item exists, update quantity and save to Firestore
        for (CartModel cartItem : updatedCart) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                saveItemToFirestore(cartItem);
                found = true;
                break;
            }
        }

        // If item doesn't exist, add new item and save to Firestore
        if (!found) {
            updatedCart.add(item);
            saveItemToFirestore(item);
        }

        cartItems.setValue(updatedCart);
        recalculatePrices();
    }

    public void removeItem(CartModel item) {
        List<CartModel> updatedCart = new ArrayList<>(cartItems.getValue());
        updatedCart.remove(item);
        cartItems.setValue(updatedCart);
        recalculatePrices();

        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(item.getName())
                    .delete()
                    .addOnSuccessListener(unused -> Log.d("CartViewModel", "Item deleted"))
                    .addOnFailureListener(e -> Log.e("CartViewModel", "Item delete failed", e));
        }
    }

    public void updateQuantity(CartModel item, Long quantity) {
        if (quantity <= 0) {
            removeItem(item);
            return;
        }

        List<CartModel> updatedCart = new ArrayList<>(cartItems.getValue());
        for (CartModel cartItem : updatedCart) {
            if (cartItem.getName().equals(item.getName())) {
                cartItem.setQuantity(quantity);
                break;
            }
        }

        cartItems.setValue(updatedCart);
        recalculatePrices();

        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(item.getName())
                    .update("quantity", quantity);
        }
    }

    public void applyVoucher(String voucherCode) {
        if (voucherCodes.containsKey(voucherCode)) {
            appliedVoucher.setValue(voucherCode);
        } else {
            appliedVoucher.setValue(null);
        }
        recalculatePrices();
    }

    private void recalculatePrices() {
        List<CartModel> currentItems = cartItems.getValue();
        double subtotalValue = currentItems.stream().mapToDouble(CartModel::getSubtotal).sum();
        double discount = appliedVoucher.getValue() != null ? voucherCodes.get(appliedVoucher.getValue()) : 0.0;
        double taxValue = subtotalValue * TAX_RATE;
        double totalValue = (subtotalValue + DELIVERY_FEE + taxValue) * (1 - discount);

        subtotal.setValue(subtotalValue);
        tax.setValue(taxValue);
        total.setValue(totalValue);

        saveCartToFirestore();
    }

    private void saveItemToFirestore(CartModel item) {
        if (userId == null) return;

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("imageUrl", item.getImageUrl());
        itemMap.put("price", item.getPrice());
        itemMap.put("quantity", item.getQuantity());

        db.collection("users")
                .document(userId)
                .collection("cart")
                .document(item.getName())
                .set(itemMap)
                .addOnFailureListener(e -> Log.e("CartViewModel", "Error saving item to Firestore", e));
    }

    public void saveCartToFirestore() {
        if (userId == null) return;

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("items", cartItems.getValue());
        cartData.put("subtotal", subtotal.getValue());
        cartData.put("tax", tax.getValue());
        cartData.put("total", total.getValue());

        db.collection("carts")
                .document(userId)
                .set(cartData)
                .addOnFailureListener(e -> Log.e("CartViewModel", "Error saving cart to Firestore", e));
    }

    public void loadCartFromFirestore() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CartModel> loadedItems = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("imageUrl");
                        Long price = doc.getLong("price");
                        Long quantity = doc.getLong("quantity");

                        loadedItems.add(new CartModel(imageUrl, name, price, quantity));
                    }

                    cartItems.setValue(loadedItems);
                    recalculatePrices();
                });
    }

    public boolean placeOrder() {
        if (userId == null) return false;

        List<CartModel> currentItems = cartItems.getValue();
        if (currentItems == null || currentItems.isEmpty()) {
            Log.w("CartViewModel", "Cannot place order: cart is empty.");
            return false;
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("items", currentItems);
        orderData.put("subtotal", subtotal.getValue());
        orderData.put("tax", tax.getValue());
        orderData.put("total", total.getValue());
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("status", "Pending");

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    clearLocalCart();
                    removeCartItemsFromFirestore();
                })
                .addOnFailureListener(e -> Log.e("CartViewModel", "Order failed", e));
        return true;
    }

    private void clearLocalCart() {
        cartItems.setValue(new ArrayList<>());
        subtotal.setValue(0.0);
        tax.setValue(0.0);
        total.setValue(0.0);
        appliedVoucher.setValue(null);
    }

    private void removeCartItemsFromFirestore() {
        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    Log.d("CartViewModel", "Cart cleared after order");
                })
                .addOnFailureListener(e -> Log.e("CartViewModel", "Error clearing cart after order", e));
    }

    public void calculatePriceDetails(CartModel cartItem) {
        List<CartModel> currentItems = cartItems.getValue();
        double subtotalValue = currentItems.stream().mapToDouble(CartModel::getSubtotal).sum();
        double discount = appliedVoucher.getValue() != null ? voucherCodes.get(appliedVoucher.getValue()) : 0.0;
        double taxValue = subtotalValue * TAX_RATE;
        double totalValue = (subtotalValue + DELIVERY_FEE + taxValue) * (1 - discount);

        subtotal.setValue(subtotalValue);
        tax.setValue(taxValue);
        total.setValue(totalValue);

        saveCartToFirestore();
    }
}
