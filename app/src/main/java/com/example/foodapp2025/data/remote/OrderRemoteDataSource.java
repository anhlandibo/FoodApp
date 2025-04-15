package com.example.foodapp2025.data.remote;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.UserModel;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRemoteDataSource {
    private final CollectionReference orderCollection;

    public OrderRemoteDataSource() {
        orderCollection = FirebaseFirestore.getInstance().collection("orders");
    }

    // Place an order
    public void placeOrder(UserModel user, List<CartModel> cartItems) {
        if (cartItems.isEmpty()) {
            Log.e("OrderRemoteDataSource", "Cart is empty, cannot place order.");
            return;
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", user.getUid());
        orderData.put("location", user.getLocation());
        orderData.put("timestamp", FieldValue.serverTimestamp());

        List<Map<String, Object>> itemList = new ArrayList<>();
        double totalPrice = 0;

        for (CartModel item : cartItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("name", item.getName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("price", item.getPrice());
            itemList.add(itemData);
            totalPrice += item.getTotal();
        }

        orderData.put("items", itemList);
        orderData.put("totalPrice", totalPrice);

        orderCollection.add(orderData)
                .addOnSuccessListener(documentReference -> Log.d("OrderRemoteDataSource", "Order placed successfully!"))
                .addOnFailureListener(e -> Log.e("OrderRemoteDataSource", "Error placing order", e));
    }

    // Get orders for a specific user
    public LiveData<List<Map<String, Object>>> getOrders(String userId) {
        MutableLiveData<List<Map<String, Object>>> liveData = new MutableLiveData<>();

        orderCollection.whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> orders = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        orders.add(doc.getData());
                    }
                    liveData.setValue(orders);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                    Log.e("OrderRemoteDataSource", "Error getting orders", e);
                });

        return liveData;
    }
}