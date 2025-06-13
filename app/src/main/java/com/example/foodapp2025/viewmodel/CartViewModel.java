package com.example.foodapp2025.viewmodel;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.utils.discount.Discount;
import com.example.foodapp2025.utils.discount.DiscountRegistry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartViewModel extends ViewModel {
    private static final double TAX_RATE = 0.05;
    private static final double DELIVERY_FEE = 5.0; // Already a double
    private final MutableLiveData<Double> delivery = new MutableLiveData<>(5.0); // Already a double
    private final MutableLiveData<VoucherModel> voucher = new MutableLiveData<>();
    private final MutableLiveData<String> voucherError = new MutableLiveData<>(null);
    private final MutableLiveData<String> appliedVoucher = new MutableLiveData<>(null);
    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0); // Already a double
    private final MutableLiveData<Double> tax = new MutableLiveData<>(0.0);       // Already a double
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);      // Already a double
    private final MutableLiveData<String> note = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> orderPlaced = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cartCleared = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastCreatedOrderId = new MutableLiveData<>(null);
    private final MutableLiveData<Double> discountAmount = new MutableLiveData<>(0.0); // Already a double
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
    private final String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

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

    public CartViewModel() {
        loadCartFromFirestore();
        loadUserAddressFromFirestore();
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

    /**
     * Adds an item to the cart. If an item with the same name AND note already exists,
     * its quantity is updated. Otherwise, a new item is added.
     * @param item The CartModel item to add.
     */
    public void addItem(CartModel item) {
        if (item.getQuantity() <= 0 || userId == null) return;
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        boolean found = false;
        for (CartModel ci : updated) {
            // Check if item exists with same name AND same note
            if (ci.getName().equals(item.getName()) &&
                    Objects.equals(ci.getNote(), item.getNote())) { // Use Objects.equals for null-safe comparison
                ci.setQuantity(ci.getQuantity() + item.getQuantity());
                saveItemToFirestore(ci); // Update existing item in Firestore
                found = true;
                break;
            }
        }
        if (!found) {
            updated.add(item);
            saveItemToFirestore(item); // Save new item to Firestore
        }
        cartItems.setValue(updated);
        recalculatePrices();
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

    public void removeItem(CartModel item) {
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        updated.remove(item);
        cartItems.setValue(updated);
        recalculatePrices();
        if (userId != null) {
            // Firestore document ID for cart items should be unique per item,
            // perhaps a combination of name and note, or a unique ID.
            // For simplicity, using a name-based ID. If notes differentiate items,
            // you might need a more complex ID or separate documents.
            // Current implementation assumes cart item name is unique or notes are part of name for unique ID.
            // For robust solution, consider a unique ID for each CartModel.
            String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(docId) // Using a more unique ID
                    .delete();
        }
    }

    /**
     * Updates the quantity of a specific item in the cart.
     * Note: This method currently assumes item is uniquely identified by its name.
     * If notes make items unique, you'd need to adapt this or pass a unique ID.
     * @param item The CartModel item to update (used for identification).
     * @param quantity The new quantity.
     */
    public void updateQuantity(CartModel item, Long quantity) {
        if (quantity <= 0) {
            removeItem(item);
            return;
        }
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        boolean found = false;
        for (CartModel ci : updated) {
            // Check if item exists with same name AND same note
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
            // Update Firestore with the new quantity
            String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(docId)
                    .update("quantity", quantity);
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
                        Log.d(TAG, String.format(
                                "Loaded VM: code=%s, active=%b, exp=%s",
                                Objects.requireNonNull(vm).getCode(), vm.isActive(), vm.getExpiryDate()));

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
                        } else {
                            // thành công
                            Log.d(TAG, "Voucher valid and applied!");
                            appliedVoucher.setValue(code);
                            voucher.setValue(vm);
                            voucherError.setValue(null);
                        }
                    }
                    recalculatePrices();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching voucher", e);
                    voucherError.setValue("Error while checking voucher.");
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
                && curSub >= v.getMinOrderValue();
        Log.d("CartVM", "isVoucherValid=" + valid
                + " subtotal=" + curSub + " minOrder=" + v.getMinOrderValue());
        return valid;
    }

    public void recalculatePrices() {
        // 1. Tính tổng phụ (subtotal) từ các mặt hàng trong giỏ
        List<CartModel> items = cartItems.getValue();
        double sub = items.stream().mapToDouble(CartModel::getSubtotal).sum();

        // 2. Tính thuế dựa trên subtotal
        double taxAmt = sub * TAX_RATE;

        // 3. Tính tổng số tiền TRƯỚC KHI áp dụng giảm giá,
        // bao gồm subtotal, phí giao hàng và thuế.
        // Đây là "total" mà bạn muốn dùng để tính discountAmt.
        double totalBeforeDiscount = sub + DELIVERY_FEE + taxAmt;

        double discountAmt = 0.0;
        VoucherModel vm = voucher.getValue();
        if (vm != null && isVoucherValid(vm)) {
            Discount strat = DiscountRegistry.get(vm.getDiscountType());
            if (strat != null) {
                // 4. Áp dụng giảm giá dựa trên 'totalBeforeDiscount'
                discountAmt = strat.applyDiscount(totalBeforeDiscount, vm);
            } else {
                Log.w("CartVM", "No strategy for type=" + vm.getDiscountType());
            }
        }

        // 5. Tính tổng số tiền cuối cùng sau khi đã áp dụng giảm giá
        double totalAmt = totalBeforeDiscount - discountAmt;

        // Cập nhật các LiveData hoặc thuộc tính
        subtotal.setValue(sub);
        tax.setValue(taxAmt);
        // Đảm bảo tổng số tiền không âm (ví dụ: nếu giảm giá quá lớn)
        total.setValue(Math.max(totalAmt, 0));
        discountAmount.setValue(discountAmt);

        // Lưu giỏ hàng vào Firestore (optional, as individual item saves are done in addItem/updateQuantity)
         saveCartToFirestore(); // This can be removed if individual item saves are sufficient
    }

    public boolean isCartEmpty() {
        return Objects.requireNonNull(cartItems.getValue()).isEmpty();
    }

    // Firestore helpers
    /**
     * Saves or updates a single cart item to Firestore.
     * Uses a document ID based on item name and a hash of its note to ensure uniqueness.
     * @param item The CartModel item to save.
     */
    private void saveItemToFirestore(CartModel item) {
        if (userId == null) return;
        Map<String, Object> m = new HashMap<>();
        m.put("name", item.getName());
        m.put("imageUrl", item.getImageUrl());
        m.put("price", item.getPrice());
        m.put("quantity", item.getQuantity());
        m.put("note", item.getNote()); // **ADDED NOTE FIELD**

        // Create a unique document ID based on name and note hash
        // This is important if you want "Item A (no sugar)" and "Item A (extra sugar)"
        // to be separate documents in Firestore.
        String docId = item.getName() + (item.getNote() != null ? "_" + item.getNote().hashCode() : "");

        db.collection("users")
                .document(userId)
                .collection("cart")
                .document(docId) // Use the unique document ID
                .set(m);
    }

    /**
     * Saves the entire cart summary to a specific 'carts' collection.
     * This is separate from individual item saves in the 'users/{userId}/cart' subcollection.
     * @deprecated Consider if this method is still needed if individual items are managed in the subcollection.
     */
    @Deprecated
    public void saveCartToFirestore() {
        if (userId == null) return;
        Map<String, Object> data = new HashMap<>();
        // Note: 'items' here might duplicate data if individual items are in a subcollection
        // Consider saving only summary data like subtotal, tax, total here.
        // For 'items', you might need to serialize your CartModel list to a simple Map list if needed
        List<Map<String, Object>> itemsAsMaps = new ArrayList<>();
        for (CartModel item : Objects.requireNonNull(cartItems.getValue())) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("subtotal", item.getSubtotal());
            itemMap.put("note", item.getNote()); // **ADDED NOTE FIELD**
            itemsAsMaps.add(itemMap);
        }
        data.put("items", itemsAsMaps);
        data.put("subtotal", subtotal.getValue());
        data.put("tax", tax.getValue());
        data.put("total", total.getValue());
        data.put("note", note.getValue());
        db.collection("carts")
                .document(userId)
                .set(data);
    }

    /**
     * Loads cart items from the user's subcollection in Firestore.
     */
    public void loadCartFromFirestore() {
        if (userId == null) return;
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
                        String note = doc.getString("note"); // **ADDED NOTE FIELD**
                        loaded.add(new CartModel(url, name, price, Objects.requireNonNull(qty), note)); // **UPDATED CONSTRUCTOR**
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

    /**
     * Clears all items from the user's cart in Firestore and resets local cart state.
     */
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
                                    // Reset local state after successful Firestore clear
                                    cartItems.setValue(new ArrayList<>());
                                    subtotal.setValue(0.0);
                                    tax.setValue(0.0);
                                    total.setValue(0.0);
                                    appliedVoucher.setValue(null);
                                    cartCleared.setValue(true);
                                    discountAmount.setValue(0.0); // Reset discount
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
            // If userId is null, clear local state immediately
            cartItems.setValue(new ArrayList<>());
            subtotal.setValue(0.0);
            tax.setValue(0.0);
            total.setValue(0.0);
            delivery.setValue(5.0);
            appliedVoucher.setValue(null);
            cartCleared.setValue(true);
            discountAmount.setValue(0.0); // Reset discount
        }
    }

    /**
     * Places a new order in Firestore.
     * @param paymentMethod The payment method (e.g., "cod", "card").
     * @param totalAmount The final total amount of the order (should match `total.getValue()`).
     */
    public void placeOrder(String paymentMethod, Double totalAmount) {
        if (userId == null || Objects.requireNonNull(cartItems.getValue()).isEmpty()) {
            Log.e(TAG, "Failed to place order: userId is null or cart is empty.");
            orderPlaced.setValue(false);
            lastCreatedOrderId.setValue(null);
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
            itemMap.put("note", item.getNote()); // **ADDED NOTE FIELD**
            itemsToSave.add(itemMap);
        }
        order.put("items", itemsToSave);
        //order.put("note", note.getValue());
        order.put("subtotal", subtotal.getValue());
        order.put("tax", tax.getValue());
        order.put("total", total.getValue());
        order.put("discountAmount", discountAmount.getValue());
        order.put("timestamp", System.currentTimeMillis());

        String deliveryAddress = userAddress.getValue();
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            order.put("deliveryAddress", deliveryAddress);
        } else {
            Log.w(TAG, "User address is null or empty when placing order.");
        }
        VoucherModel currentVoucher = voucher.getValue();
        if (currentVoucher != null && appliedVoucher.getValue() != null) {
            Map<String, Object> voucherDetails = new HashMap<>();
            voucherDetails.put("code", currentVoucher.getCode());
            voucherDetails.put("type", currentVoucher.getDiscountType());
            voucherDetails.put("value", currentVoucher.getDiscountValue());
            order.put("appliedVoucherDetails", voucherDetails);
        }

        String status;
        String paymentStatus;

        if ("cod".equals(paymentMethod)) {
            status = "pending";
            paymentStatus = "unpaid";
            order.put("paymentStatus", paymentStatus);
            order.put("status", status);
            order.put("paymentMethod", "cod");
            order.put("reportStatus", 0);
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        String newOrderId = ref.getId();
                        Log.d(TAG, "Order placed successfully with ID: " + newOrderId + " and method: " + paymentMethod);
                        clearCartInFirestoreAndLocal(); // Clear cart after successful COD order
                        orderPlaced.setValue(true);
                        // - Tisn here- maybelastCreatedOrderId.setValue(newOrderId); // Set the new order ID
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing COD order.", e);
                        orderPlaced.setValue(false);
                        lastCreatedOrderId.setValue(null);
                    });
        } else if ("card_low_amount".equals(paymentMethod) || "card_zero_amount".equals(paymentMethod)) {
            // This payment path usually means the payment is already handled by the app
            status = "pending";
            paymentStatus = "paid";
            order.put("status", status);
            order.put("paymentStatus", paymentStatus);
            order.put("paymentMethod", "card");
            order.put("reportStatus", 0);
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        String newOrderId = ref.getId();
                        Log.d(TAG, "Order placed successfully with ID: " + newOrderId + " (zero/low amount).");
                        clearCartInFirestoreAndLocal(); // Clear cart after successful card order
                        orderPlaced.setValue(true);
                        // maybe here lastCreatedOrderId.setValue(newOrderId); // Set the new order ID
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing zero/low amount card order.", e);
                        orderPlaced.setValue(false);
                        lastCreatedOrderId.setValue(null);
                    });
        } else {
            // General card payment (assuming this needs external payment gateway)
            status = "pending";
            paymentStatus = "paid"; // Payment is handled externally, so mark as paid
            order.put("status", status);
            order.put("paymentStatus", paymentStatus);
            order.put("paymentMethod", paymentMethod);
            order.put("reportStatus", 0);
            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(ref -> {
                        String newOrderId = ref.getId();
                        Log.d(TAG, "Order placed successfully with ID: " + newOrderId + " for card payment.");
                        // For this path, we set lastCreatedOrderId so UI can redirect to payment gateway
                        lastCreatedOrderId.setValue(newOrderId);
                        // Do NOT clear cart here yet, wait for payment confirmation from gateway
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error placing card order.", e);
                        orderPlaced.setValue(false);
                        lastCreatedOrderId.setValue(null);
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
        } else {
            Log.d(TAG, "Voucher valid and applied!");
            appliedVoucher.setValue(vm.getCode());
            voucher.setValue(vm);
            voucherError.setValue(null);
        }

        recalculatePrices();
    }
}