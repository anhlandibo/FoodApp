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
    private static final double DELIVERY_FEE = 5.0;
    private final MutableLiveData<Double> delivery = new MutableLiveData<>(5.0);
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

    public LiveData<Double> getDiscountAmount() {return discountAmount;}


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

    public void addItem(CartModel item) {
        if (item.getQuantity() <= 0 || userId == null) return;
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        boolean found = false;
        for (CartModel ci : updated) {
            if (ci.getName().equals(item.getName())) {
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
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(item.getName())
                    .delete();
        }
    }

    public void updateQuantity(CartModel item, Long quantity) {
        if (quantity <= 0) {
            removeItem(item);
            return;
        }
        List<CartModel> updated = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        for (CartModel ci : updated) {
            if (ci.getName().equals(item.getName())) {
                ci.setQuantity(quantity);
                break;
            }
        }
        cartItems.setValue(updated);
        recalculatePrices();
        if (userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(item.getName())
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

        // Lưu giỏ hàng vào Firestore
        saveCartToFirestore();
    }

    public boolean isCartEmpty(){
        if(cartItems.getValue().isEmpty()) return true;
        else return false;
    }

    // Firestore helpers
    private void saveItemToFirestore(CartModel item) {
        if (userId == null) return;
        Map<String, Object> m = new HashMap<>();
        m.put("name", item.getName());
        m.put("imageUrl", item.getImageUrl());
        m.put("price", item.getPrice());
        m.put("quantity", item.getQuantity());
        db.collection("users")
                .document(userId)
                .collection("cart")
                .document(item.getName())
                .set(m);
    }

    public void saveCartToFirestore() {
        if (userId == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("items", cartItems.getValue());
        data.put("subtotal", subtotal.getValue());
        data.put("tax", tax.getValue());
        data.put("total", total.getValue());
        db.collection("carts")
                .document(userId)
                .set(data);
    }

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
                        loaded.add(new CartModel(url, name, price, Objects.requireNonNull(qty)));
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
                                    cartItems.setValue(new ArrayList<>());
                                    subtotal.setValue(0.0);
                                    tax.setValue(0.0);
                                    total.setValue(0.0);
                                    appliedVoucher.setValue(null);
                                    cartCleared.setValue(true);
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
            cartItems.setValue(new ArrayList<>());
            subtotal.setValue(0.0);
            tax.setValue(0.0);
            total.setValue(0.0);
            delivery.setValue(5.0);
            appliedVoucher.setValue(null);
            cartCleared.setValue(true);
        }
    }

    public void placeOrder(String paymentMethod) {
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
            itemsToSave.add(itemMap);
        }
        order.put("items", itemsToSave);

        order.put("subtotal", subtotal.getValue());
        order.put("tax", tax.getValue());
        order.put("total", total.getValue());
        order.put("timestamp", System.currentTimeMillis());

        String status;
        String paymentStatus;

        if ("card".equals(paymentMethod)) {
            status = "pending";
            paymentStatus = "unpaid";
        } else { // "cod"
            status = "pending";
            paymentStatus = "unpaid";
        }

        order.put("status", status);
        order.put("paymentStatus", paymentStatus);
        order.put("paymentMethod", paymentMethod);

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(ref -> {
                    String newOrderId = ref.getId();
                    Log.d(TAG, "Order placed successfully with ID: " + newOrderId + " and method: " + paymentMethod);

                    if ("cod".equals(paymentMethod)) {
                        clearCartInFirestoreAndLocal();
                        orderPlaced.setValue(true);
                    } else { // "card"
                        lastCreatedOrderId.setValue(newOrderId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error placing order.", e);
                    orderPlaced.setValue(false);
                    lastCreatedOrderId.setValue(null);
                });
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