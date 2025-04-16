package com.example.foodapp2025.data.remote;

import android.util.Log;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CartRemoteDataSource {

    public void addItemToCart(String userId, String itemId, int quantity) {
        DocumentReference cartItemRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("cart")
                .document(itemId);

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("quantity", quantity);
        cartData.put("itemID", itemId);// Store only quantity

        cartItemRef.set(cartData)
                .addOnSuccessListener(aVoid -> Log.d("Cart", "Item added successfully"))
                .addOnFailureListener(e -> Log.e("Cart", "Error adding item", e));
    }

    public void placeOrder(String userId, GeoPoint location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");

        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                List<Map<String, Object>> itemList = new ArrayList<>();
                double[] totalPrice = {0};

                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot doc : documents) {
                    String itemId = doc.getId();
                    int quantity = ((Long) doc.get("quantity")).intValue();

                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("itemId", itemId);
                    itemData.put("quantity", quantity);
                    itemList.add(itemData);

                    // Get item price
                    db.collection("items").document(itemId).get().addOnSuccessListener(itemSnapshot -> {
                        if (itemSnapshot.exists()) {
                            int price = itemSnapshot.getLong("price").intValue();
                            totalPrice[0] += price * quantity;

                            // After collecting all prices, create the order and clear cart
                            if (itemList.size() == documents.size()) {
                                Map<String, Object> orderData = new HashMap<>();
                                orderData.put("userId", userId);
                                orderData.put("items", itemList);
                                orderData.put("totalPrice", totalPrice[0]);
                                orderData.put("location", location);
                                orderData.put("timestamp", FieldValue.serverTimestamp());
                                orderData.put("status", "Pending");

                                db.collection("orders").add(orderData)
                                        .addOnSuccessListener(orderDoc -> {
                                            Log.d("Order", "Order placed successfully!");
                                            clearCart(userId);  // ✅ Clear the cart
                                        })
                                        .addOnFailureListener(e -> Log.e("Order", "Error placing order", e));
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(e -> Log.e("Cart", "Error fetching cart", e));
    }

    public void removeItemFromCart(String userId, String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference itemRef = db.collection("users")
                .document(userId)
                .collection("cart")
                .document(itemId);

        itemRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Cart", "Item removed successfully."))
                .addOnFailureListener(e -> Log.e("Cart", "Failed to remove item", e));
    }

    public void clearCart(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");

        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                batch.delete(doc.getReference());
            }

            batch.commit()
                    .addOnSuccessListener(unused -> Log.d("Cart", "Cart successfully cleared after order."))
                    .addOnFailureListener(e -> Log.e("Cart", "Failed to clear cart after order", e));
        });
    }

}
