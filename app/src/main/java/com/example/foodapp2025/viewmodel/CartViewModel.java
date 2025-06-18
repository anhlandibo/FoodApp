package com.example.foodapp2025.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.utils.discount.Discount;
import com.example.foodapp2025.utils.discount.DiscountRegistry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartViewModel extends ViewModel {
    private static final String TAG = "CartViewModel";
    private static final double TAX_RATE = 0.05;
    private static final double DELIVERY_FEE = 5.0;

    private final MutableLiveData<VoucherModel> voucher = new MutableLiveData<>();
    private final MutableLiveData<String> voucherError = new MutableLiveData<>(null);
    private final MutableLiveData<String> appliedVoucher = new MutableLiveData<>(null);
    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> tax = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> orderPlaced = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cartCleared = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastCreatedOrderId = new MutableLiveData<>(null);
    private final MutableLiveData<Double> discountAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> userAddress = new MutableLiveData<>(null);

    public LiveData<Double> getDiscountAmount() {
        return discountAmount;
    }
    public LiveData<String> getUserAddress() {
        return userAddress;
    }

    public LiveData<String> getLastCreatedOrderId() {
        return lastCreatedOrderId;
    }

    public LiveData<Boolean> getCartCleared() {
        return cartCleared;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId;

    public CartViewModel() {
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        loadCartFromFirestore();
        loadUserAddressFromFirestore();
    }

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

    public LiveData<Boolean> getOrderPlaced() {
        return orderPlaced;
    }

    public LiveData<String> getVoucherError() {
        return voucherError;
    }

    public LiveData<String> getAppliedVoucher() {
        return appliedVoucher;
    }

    public LiveData<VoucherModel> getVoucher() {
        return voucher;
    }

    public void resetLastCreatedOrderId() {
        lastCreatedOrderId.setValue(null);
    }

    public void resetOrderPlacedStatus() {
        orderPlaced.setValue(false);
    }

    public void resetCartClearedStatus() {
        cartCleared.setValue(false);
    }

    public void loadUserAddressFromFirestore() {
        if (userId == null) {
            Log.e(TAG, "Cannot load user address: userId is null.");
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        userAddress.setValue(address);
                        Log.d(TAG, "User address loaded: " + address);
                    } else {
                        Log.d(TAG, "User document does not exist for ID: " + userId);
                        userAddress.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user address from Firestore.", e);
                    userAddress.setValue(null);
                });
    }

    public void addItem(CartModel item) {
        if (item.getQuantity() <= 0 || userId == null) {
            if (userId == null) Log.w(TAG, "Cannot add item: userId is null.");
            return;
        }
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        boolean found = false;
        for (CartModel ci : updated) {
            if (ci.getName().equals(item.getName()) &&
                    Objects.equals(ci.getNote(), item.getNote())) {
                ci.setQuantity(ci.getQuantity() + item.getQuantity());
                saveItemToFirestore(ci);
                found = true;
                break;
            }
        }
        if (!found) {
            updated.add(item);
            saveItemToFirestore(item);
        }
        cartItems.setValue(updated);
        recalculatePrices();
    }

    public void removeItem(CartModel item) {
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        updated.remove(item);
        cartItems.setValue(updated);
        recalculatePrices();
        if (userId != null) {
            String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(docId)
                    .delete()
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting item from Firestore: " + e.getMessage()));
        }
    }

    public void updateQuantity(CartModel item, Long quantity) {
        if (quantity <= 0) {
            removeItem(item);
            return;
        }
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        boolean found = false;
        for (CartModel ci : updated) {
            if (ci.getName().equals(item.getName()) &&
                    Objects.equals(ci.getNote(), item.getNote())) {
                ci.setQuantity(quantity);
                found = true;
                break;
            }
        }
        cartItems.setValue(updated);
        recalculatePrices();
        if (userId != null && found) {
            String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(docId)
                    .update("quantity", quantity)
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating item quantity in Firestore: " + e.getMessage()));
        }
    }

    public void applyVoucher(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            voucherError.setValue("Voucher code is empty.");
            appliedVoucher.setValue(null);
            voucher.setValue(null);
            recalculatePrices();
            return;
        }

        final String code = rawCode.trim().toUpperCase();
        Log.d(TAG, "applyVoucher(): trying code = \"" + code + "\"");

        db.collection("vouchers")
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(qs -> {
                    Log.d(TAG, "Firestore returned docsCount=" + qs.size());
                    if (qs.isEmpty()) {
                        Log.d(TAG, "No voucher found for code=" + code);
                        voucherError.setValue("Voucher is not existed.");
                        appliedVoucher.setValue(null);
                        voucher.setValue(null);
                    } else {
                        VoucherModel vm = qs.getDocuments().get(0).toObject(VoucherModel.class);
                        applyVoucherObject(vm);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching voucher", e);
                    voucherError.setValue("Error while checking voucher.");
                    appliedVoucher.setValue(null);
                    voucher.setValue(null);
                    recalculatePrices();
                });
    }

    public void applyVoucherObject(VoucherModel vm) {
        if (vm == null) {
            appliedVoucher.setValue(null);
            voucher.setValue(null);
            recalculatePrices();
            return;
        }

        Log.d(TAG, String.format(
                "applyVoucherObject: code=%s, active=%b, exp=%s",
                vm.getCode(), vm.isActive(), vm.getExpiryDate()));

        if (userId == null) {
            voucherError.setValue("Please log in to apply vouchers.");
            appliedVoucher.setValue(null);
            voucher.setValue(null);
            recalculatePrices();
            return;
        }

        // Check if user has already used this voucher
        db.collection("users").document(userId).collection("usedVouchers")
                .document(vm.getCode())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Voucher " + vm.getCode() + " has already been used by user " + userId);
                        voucherError.setValue("You have already used this voucher.");
                        appliedVoucher.setValue(null);
                        voucher.setValue(null);
                        recalculatePrices();
                    } else {
                        double currentSubtotalValue = subtotal.getValue() != null ? subtotal.getValue() : 0.0;

                        if (!vm.isActive()) {
                            Log.d(TAG, "Voucher isActive=false");
                            voucherError.setValue("Voucher is not active now.");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        } else if (vm.isExpired()) {
                            Log.d(TAG, "Voucher isExpired=true (expiryDate=" + vm.getExpiryDate() + ")");
                            voucherError.setValue("Voucher has been expired.");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        } else if (currentSubtotalValue < vm.getMinOrderValue()) {
                            Log.d(TAG, "Voucher minOrderValue not met. Current subtotal: " + currentSubtotalValue + ", Required: " + vm.getMinOrderValue());
                            voucherError.setValue("Order total does not meet the minimum required for this voucher (Min: " + String.format("%,.0f$", vm.getMinOrderValue()) + ")");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        } else if (vm.getUsedCount() >= vm.getUsageLimit()) {
                            Log.d(TAG, "Voucher usage limit reached. Used: " + vm.getUsedCount() + ", Limit: " + vm.getUsageLimit());
                            voucherError.setValue("This voucher has reached its usage limit.");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        } else {
                            Log.d(TAG, "Voucher valid and applied!");
                            appliedVoucher.setValue(vm.getCode());
                            voucher.setValue(vm);
                            voucherError.setValue(null);

                            // Increment usedCount in Firestore (this still counts total uses across all users)
                            // We will mark user-specific usage on order placement.
                            db.collection("vouchers")
                                    .whereEqualTo("code", vm.getCode())
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            String voucherDocId = querySnapshot.getDocuments().get(0).getId();
                                            db.collection("vouchers").document(voucherDocId)
                                                    .update("usedCount", FieldValue.increment(1))
                                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Voucher usedCount incremented successfully via applyVoucherObject."))
                                                    .addOnFailureListener(e -> Log.e(TAG, "Error incrementing voucher usedCount via applyVoucherObject", e));
                                        } else {
                                            Log.e(TAG, "Voucher document not found for code: " + vm.getCode() + " during usedCount increment.");
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error querying for voucher document to increment usedCount.", e));
                        }
                        recalculatePrices();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user's used vouchers: " + e.getMessage());
                    voucherError.setValue("Error checking voucher usage.");
                    appliedVoucher.setValue(null);
                    voucher.setValue(null);
                    recalculatePrices();
                });
    }

    private boolean isVoucherValid(VoucherModel v) {
        Date now = new Date();
        double curSub = subtotal.getValue() != null ? subtotal.getValue() : 0;
        boolean valid = v.isActive()
                && (v.getStartDate() == null || !now.before(v.getStartDate()))
                && (v.getExpiryDate() == null || !now.after(v.getExpiryDate()))
                && curSub >= v.getMinOrderValue()
                && v.getUsedCount() < v.getUsageLimit();
        Log.d(TAG, "isVoucherValid=" + valid
                + " subtotal=" + curSub + " minOrder=" + v.getMinOrderValue()
                + " usedCount=" + v.getUsedCount() + " usageLimit=" + v.getUsageLimit());
        return valid;
    }

    public void recalculatePrices() {
        List<CartModel> items = cartItems.getValue();
        if (items == null) {
            items = new ArrayList<>();
        }
        double sub = items.stream().mapToDouble(CartModel::getSubtotal).sum();

        double taxAmt = sub * TAX_RATE;

        double totalBeforeDiscount = sub + DELIVERY_FEE + taxAmt;

        double discountAmt = 0.0;
        VoucherModel vm = voucher.getValue();
        if (vm != null && isVoucherValid(vm)) {
            Discount strat = DiscountRegistry.get(vm.getDiscountType());
            if (strat != null) {
                discountAmt = strat.applyDiscount(totalBeforeDiscount, vm);
                if (discountAmt > totalBeforeDiscount) {
                    discountAmt = totalBeforeDiscount;
                }
            } else {
                Log.w(TAG, "No strategy for type=" + vm.getDiscountType());
            }
        } else if (vm != null) {
            Log.d(TAG, "Voucher " + vm.getCode() + " is no longer valid during recalculation. Resetting.");
            appliedVoucher.setValue(null);
            voucher.setValue(null);
            voucherError.setValue("Applied voucher is no longer valid.");
        }

        double totalAmt = totalBeforeDiscount - discountAmt;

        subtotal.setValue(sub);
        tax.setValue(taxAmt);
        total.setValue(Math.max(totalAmt, 0));
        discountAmount.setValue(discountAmt);
    }

    public boolean isCartEmpty() {
        return Objects.requireNonNull(cartItems.getValue()).isEmpty();
    }

    private void saveItemToFirestore(CartModel item) {
        if (userId == null) return;
        Map<String, Object> m = new HashMap<>();
        m.put("name", item.getName());
        m.put("imageUrl", item.getImageUrl());
        m.put("price", item.getPrice());
        m.put("quantity", item.getQuantity());
        m.put("note", item.getNote());

        String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");

        db.collection("users")
                .document(userId)
                .collection("cart")
                .document(docId)
                .set(m)
                .addOnFailureListener(e -> Log.e(TAG, "Error saving item to Firestore: " + e.getMessage()));
    }

    public void loadCartFromFirestore() {
        if (userId == null) {
            Log.w(TAG, "Cannot load cart: userId is null.");
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(qs -> {
                    List<CartModel> loaded = new ArrayList<>();
                    for (var doc : qs.getDocuments()) {
                        String name = doc.getString("name");
                        String url = doc.getString("imageUrl");
                        Double price = doc.getDouble("price");
                        if (price == null) {
                            Long priceLong = doc.getLong("price");
                            price = priceLong != null ? priceLong.doubleValue() : 0.0;
                        }
                        Long qty = doc.getLong("quantity");
                        String note = doc.getString("note");
                        loaded.add(new CartModel(url, name, price, Objects.requireNonNull(qty), note));
                    }
                    cartItems.setValue(loaded);
                    recalculatePrices();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart from Firestore.", e);
                    cartItems.setValue(new ArrayList<>());
                    recalculatePrices();
                });
    }

    public void clearCartInFirestoreAndLocal() {
        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .get()
                    .addOnSuccessListener(cartSnapshot -> {
                        WriteBatch batch = db.batch();
                        for (var doc : cartSnapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cart successfully cleared from Firestore.");
                                    resetLocalCartState();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error clearing cart from Firestore.", e);
                                    cartCleared.setValue(false);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching cart documents to clear.", e);
                        cartCleared.setValue(false);
                    });
        } else {
            resetLocalCartState();
        }
    }

    private void resetLocalCartState() {
        cartItems.setValue(new ArrayList<>());
        subtotal.setValue(0.0);
        tax.setValue(0.0);
        total.setValue(0.0);
        voucher.setValue(null);
        appliedVoucher.setValue(null);
        discountAmount.setValue(0.0);
        cartCleared.setValue(true);
    }

    @Deprecated
    public void saveCartToFirestore() {
        if (userId == null) return;
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> itemsAsMaps = new ArrayList<>();
        for (CartModel item : Objects.requireNonNull(cartItems.getValue())) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            itemMap.put("note", item.getNote());
            itemsAsMaps.add(itemMap);
        }
        data.put("items", itemsAsMaps);
        data.put("subtotal", subtotal.getValue());
        data.put("tax", tax.getValue());
        data.put("total", total.getValue());
        db.collection("carts")
                .document(userId)
                .set(data);
    }

    public void placeOrder(String paymentMethod, Double totalAmount, String orderNote) {
        if (userId == null || Objects.requireNonNull(cartItems.getValue()).isEmpty()) {
            Log.e(TAG, "Failed to place order: userId is null or cart is empty.");
            orderPlaced.setValue(false);
            return;
        }

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);

        List<Map<String, Object>> itemsToSave = new ArrayList<>();
        for (CartModel item : Objects.requireNonNull(cartItems.getValue())) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            itemMap.put("note", item.getNote());
            itemsToSave.add(itemMap);
        }
        order.put("items", itemsToSave);

        order.put("subtotal", subtotal.getValue());
        order.put("tax", tax.getValue());
        order.put("total", total.getValue());
        order.put("discountAmount", discountAmount.getValue());
        order.put("timestamp", System.currentTimeMillis());
        order.put("deliveryFee", DELIVERY_FEE);

        String deliveryAddress = userAddress.getValue();
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            order.put("deliveryAddress", deliveryAddress);
        } else {
            Log.w(TAG, "User address is null or empty when placing order.");
        }

        if (orderNote != null && !orderNote.trim().isEmpty()) {
            order.put("orderNote", orderNote.trim());
        }

        VoucherModel currentVoucher = voucher.getValue();
        if (currentVoucher != null && appliedVoucher.getValue() != null) {
            Map<String, Object> voucherDetails = new HashMap<>();
            voucherDetails.put("code", currentVoucher.getCode());
            voucherDetails.put("type", currentVoucher.getDiscountType());
            voucherDetails.put("value", currentVoucher.getDiscountValue());
            order.put("appliedVoucherDetails", voucherDetails);
            // NEW: Call markVoucherAsUsedByUser here
            markVoucherAsUsedByUser(currentVoucher.getCode());
        }

        String status;
        String paymentStatus;

        if ("cod".equals(paymentMethod)) {
            status = "pending";
            paymentStatus = "unpaid";
            order.put("paymentStatus", paymentStatus);
            order.put("status", status);
            order.put("paymentMethod", "cod");
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        Log.d(TAG, "Order placed successfully with ID: " + ref.getId() + " and method: " + paymentMethod);
                        clearCartInFirestoreAndLocal();
                        orderPlaced.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing COD order.", e);
                        orderPlaced.setValue(false);
                    });
        } else if ("card_low_amount".equals(paymentMethod) || "card_zero_amount".equals(paymentMethod)) {
            status = "pending";
            paymentStatus = "paid";
            order.put("status", status);
            order.put("paymentStatus", paymentStatus);
            order.put("paymentMethod", "card");

            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        Log.d(TAG, "Order placed successfully with ID: " + ref.getId() + " (zero/low amount).");
                        clearCartInFirestoreAndLocal();
                        orderPlaced.setValue(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing zero/low amount card order.", e);
                        orderPlaced.setValue(false);
                    });
        } else { // "card" for external payment gateway
            status = "pending";
            paymentStatus = "unpaid";
            order.put("status", status);
            order.put("paymentStatus", paymentStatus);
            order.put("paymentMethod", paymentMethod);

            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        String newOrderId = ref.getId();
                        Log.d(TAG, "Order placed successfully with ID: " + newOrderId + " for card payment (awaiting gateway).");
                        lastCreatedOrderId.setValue(newOrderId);
                        // Do NOT clear cart here; wait for payment confirmation from gateway
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing card order.", e);
                        orderPlaced.setValue(false);
                    });
        }
    }

    public void deleteOrder(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            Log.e(TAG, "Attempted to delete null or empty orderId.");
            return;
        }

        db.collection("orders")
                .document(orderId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order " + orderId + " successfully deleted from Firestore.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting order " + orderId + ": " + e.getMessage());
                });
    }

    private void markVoucherAsUsedByUser(String voucherCode) {
        if (userId == null || voucherCode == null || voucherCode.isEmpty()) {
            Log.e(TAG, "Cannot mark voucher as used: userId or voucherCode is null/empty.");
            return;
        }

        Map<String, Object> usedVoucherData = new HashMap<>();
        usedVoucherData.put("timestamp", FieldValue.serverTimestamp());
        usedVoucherData.put("voucherCode", voucherCode);

        db.collection("users").document(userId).collection("usedVouchers")
                .document(voucherCode)
                .set(usedVoucherData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Voucher '" + voucherCode + "' marked as used by user " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error marking voucher '" + voucherCode + "' as used by user " + userId + ": " + e.getMessage()));
    }
}