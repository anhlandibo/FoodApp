package com.example.foodapp2025.viewmodel;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.WriteBatch; // Import WriteBatch

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartViewModel extends ViewModel {
    private static final double TAX_RATE = 0.05;
    private static final double DELIVERY_FEE = 20000.0;

    private final MutableLiveData<VoucherModel> voucher = new MutableLiveData<>();
    private final MutableLiveData<String> voucherError = new MutableLiveData<>(null);
    private final MutableLiveData<String> appliedVoucher = new MutableLiveData<>(null);

    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> tax = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> orderPlaced = new MutableLiveData<>(false);

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

    public LiveData<List<CartModel>> getCartItems() { return cartItems; }
    public LiveData<Double> getSubtotal()      { return subtotal;  }
    public LiveData<Double> getTax()           { return tax;       }
    public LiveData<Double> getTotal()         { return total;     }
    public LiveData<Boolean> getOrderPlaced()  { return orderPlaced; }
    public LiveData<String> getVoucherError()  { return voucherError; }
    public LiveData<String> getAppliedVoucher(){ return appliedVoucher; }
    public LiveData<VoucherModel> getVoucher() { return voucher;   }

    public CartViewModel() {
        loadCartFromFirestore();
    }

    public void addItem(CartModel item) {
        if (item.getQuantity() <= 0 || userId == null) return;
        List<CartModel> updated = new ArrayList<>(cartItems.getValue());
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
        List<CartModel> updated = new ArrayList<>(cartItems.getValue());
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
        List<CartModel> updated = new ArrayList<>(cartItems.getValue());
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
            voucherError.setValue("Mã voucher trống.");
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
                        voucherError.setValue("Voucher không tồn tại.");
                        appliedVoucher.setValue(null);
                        voucher.setValue(null);
                    } else {
                        VoucherModel vm = qs.getDocuments().get(0).toObject(VoucherModel.class);
                        Log.d(TAG, String.format(
                                "Loaded VM: code=%s, active=%b, exp=%s",
                                vm.getCode(), vm.isActive(), vm.getExpiryDate()));

                        if (!vm.isActive()) {
                            Log.d(TAG, "Voucher isActive=false");
                            voucherError.setValue("Voucher hiện không hoạt động.");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        }
                        else if (vm.isExpired()) {
                            Log.d(TAG, "Voucher isExpired=true (expiryDate=" + vm.getExpiryDate() + ")");
                            voucherError.setValue("Voucher đã hết hạn.");
                            appliedVoucher.setValue(null);
                            voucher.setValue(null);
                        }
                        else {
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
                    voucherError.setValue("Lỗi khi kiểm tra voucher.");
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
                && (v.getExpiryDate()==null || !now.after(v.getExpiryDate()))
                && curSub >= v.getMinOrderValue();
        Log.d("CartVM", "isVoucherValid=" + valid
                + " subtotal=" + curSub + " minOrder=" + v.getMinOrderValue());
        return valid;
    }

    public void recalculatePrices() {
        List<CartModel> items = cartItems.getValue();
        double sub = items.stream().mapToDouble(CartModel::getSubtotal).sum();

        double discountAmt = 0.0;
        VoucherModel vm = voucher.getValue();
        if (vm != null && isVoucherValid(vm)) {
            Discount strat = DiscountRegistry.get(vm.getDiscountType());
            if (strat != null) {
                discountAmt = strat.applyDiscount(sub, vm);
            } else {
                Log.w("CartVM", "No strategy for type=" + vm.getDiscountType());
            }
        }

        double taxAmt   = sub * TAX_RATE;
        double totalAmt = sub + DELIVERY_FEE + taxAmt - discountAmt;

        subtotal.setValue(sub);
        tax.setValue(taxAmt);
        total.setValue(Math.max(totalAmt, 0));

        saveCartToFirestore();
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
                    for (var doc: qs.getDocuments()) {
                        String name = doc.getString("name");
                        String url  = doc.getString("imageUrl");
                        Long price   = (long) doc.getLong("price").intValue();
                        Long qty     = (long) doc.getLong("quantity").intValue();
                        loaded.add(new CartModel(url, name, price, qty));
                    }
                    cartItems.setValue(loaded);
                    recalculatePrices();
                });
    }

    public boolean placeOrder() {
        if (userId == null || cartItems.getValue().isEmpty()) return false;
        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("items", cartItems.getValue());
        order.put("subtotal", subtotal.getValue());
        order.put("tax", tax.getValue());
        order.put("total", total.getValue());
        order.put("timestamp", System.currentTimeMillis());
        db.collection("orders")
                .add(order)
                .addOnSuccessListener(ref -> {
                    // Clear the cart in Firestore
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
                                                // Clear local cart data after Firestore is updated
                                                cartItems.setValue(new ArrayList<>());
                                                subtotal.setValue(0.0);
                                                tax.setValue(0.0);
                                                total.setValue(0.0);
                                                appliedVoucher.setValue(null);
                                            })
                                            .addOnFailureListener(e -> Log.e(TAG, "Error clearing cart from Firestore.", e));
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error fetching cart documents to clear.", e));
                    } else {
                        // If userId is null, just clear local data
                        cartItems.setValue(new ArrayList<>());
                        subtotal.setValue(0.0);
                        tax.setValue(0.0);
                        total.setValue(0.0);
                        appliedVoucher.setValue(null);
                    }
                });
        return true;
    }

    public void applyVoucherObject(VoucherModel vm) {
        if (vm == null) {
            voucherError.setValue("Voucher không hợp lệ.");
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
            voucherError.setValue("Voucher hiện không hoạt động.");
            appliedVoucher.setValue(null);
            voucher.setValue(null);
        }
        else if (vm.isExpired()) {
            Log.d(TAG, "Voucher isExpired=true (expiryDate=" + vm.getExpiryDate() + ")");
            voucherError.setValue("Voucher đã hết hạn.");
            appliedVoucher.setValue(null);
            voucher.setValue(null);
        }
        else {
            // Thành công
            Log.d(TAG, "Voucher valid and applied!");
            appliedVoucher.setValue(vm.getCode());
            voucher.setValue(vm);
            voucherError.setValue(null);
        }

        recalculatePrices();
    }
}