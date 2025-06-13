package com.example.foodapp2025.ui.fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.databinding.FragmentCartBinding;
import com.example.foodapp2025.ui.activity.PaymentActivity;
import com.example.foodapp2025.ui.activity.VoucherSelectionActivity;
import com.example.foodapp2025.ui.adapter.CartAdapter;
import com.example.foodapp2025.viewmodel.CartViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartViewModel cartVM;
    private CartAdapter cartAdapter;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Double pendingTotalAmount;
    private String pendingOrderIdForPayment;
    private String pendingUserId;
    private String currentPaymentMethod = null;
    private static final int CARD_PAYMENT_REQUEST_CODE = 1002;

    // ActivityResultLauncher thay thế onActivityResult
    private final ActivityResultLauncher<Intent> voucherLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            VoucherModel selectedVoucher = (VoucherModel) result.getData().getSerializableExtra("selectedVoucher");
                            if (selectedVoucher != null) {
                                cartVM.applyVoucherObject(selectedVoucher);
                            }
                        }
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle s) {
        binding = FragmentCartBinding.inflate(inflater, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        } else {
            Toast.makeText(requireContext(), "You have to login to view the cart.", Toast.LENGTH_SHORT).show();
        }
        cartVM = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        setupRecycler();
        setupObservers();
        setupListeners();
        binding.paymentMethodRadioGroup.check(R.id.radioCod);
    }

    private void setupRecycler() {
        cartAdapter = new CartAdapter(
                new ArrayList<>(),
                ci -> cartVM.updateQuantity(ci, ci.getQuantity()),
                (ci, pos) -> {
                    cartVM.removeItem(ci);
                    Toast.makeText(getContext(),"Delete item from cart.", Toast.LENGTH_SHORT).show();
                }
        );
        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartView.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        binding.voucherButton.setOnClickListener(btn -> {
            boolean isCartEmpty = cartVM.isCartEmpty();
            if(isCartEmpty) {
                Toast.makeText(requireContext(), "Please order before choosing coupons.", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(requireContext(), VoucherSelectionActivity.class);
                voucherLauncher.launch(intent);
            }
        });
        binding.button2.setOnClickListener(btn -> {
            int selectedPaymentMethodId = binding.paymentMethodRadioGroup.getCheckedRadioButtonId();

            if (Objects.requireNonNull(cartVM.getCartItems().getValue()).isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty now.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentUserId == null) {
                Toast.makeText(requireContext(), "You have to login to place order.", Toast.LENGTH_SHORT).show();
                return;
            }

            Double totalAmount = cartVM.getTotal().getValue();
            if (totalAmount == null || totalAmount < 0) {
                Toast.makeText(requireContext(), "Invalid total amount. Please check your cart again.", Toast.LENGTH_SHORT).show();
                return;
            }
            final double MIN_STRIPE_PAYMENT_AMOUNT = 0.50;

            // **Lấy ghi chú tổng thể từ EditText**
            String orderNote = binding.notesEditText.getText().toString().trim();


            if (selectedPaymentMethodId == R.id.radioCod) {
                currentPaymentMethod = "cod";
                // **Truyền orderNote vào placeOrder**
                cartVM.placeOrder("cod", totalAmount, orderNote);
                binding.voucherBanner.setVisibility(View.GONE);
                cartVM.applyVoucherObject(null);
                Toast.makeText(requireContext(), "Placing order (COD)...", Toast.LENGTH_SHORT).show();
            } else if (selectedPaymentMethodId == R.id.radioCard) {
                if (totalAmount <= MIN_STRIPE_PAYMENT_AMOUNT && totalAmount > 0){
                    currentPaymentMethod = "card_low_amount";
                    // **Truyền orderNote vào placeOrder**
                    cartVM.placeOrder("card_low_amount", totalAmount, orderNote);
                    binding.voucherBanner.setVisibility(View.GONE);
                    cartVM.applyVoucherObject(null);
                    Toast.makeText(requireContext(), "Order placed successfully", Toast.LENGTH_LONG).show();
                }
                else if (totalAmount <= 0){
                    currentPaymentMethod = "card_zero_amount";
                    // **Truyền orderNote vào placeOrder**
                    cartVM.placeOrder("card_zero_amount", totalAmount, orderNote);
                    binding.voucherBanner.setVisibility(View.GONE);
                    cartVM.applyVoucherObject(null);
                    Toast.makeText(requireContext(), "Order placed successfully", Toast.LENGTH_LONG).show();

                }
                else {
                    currentPaymentMethod = "card";
                    pendingTotalAmount = totalAmount;
                    pendingUserId = currentUserId;
                    // **Truyền orderNote vào placeOrder**
                    cartVM.placeOrder("card", totalAmount, orderNote); // Thêm totalAmount
                    binding.voucherBanner.setVisibility(View.GONE);
                    cartVM.applyVoucherObject(null);
                    Toast.makeText(requireContext(), "Processing your order for payment...", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please select a payment method.", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnRemoveVoucher.setOnClickListener(v -> {
            cartVM.applyVoucherObject(null);
            Toast.makeText(requireContext(), "Voucher removed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        cartVM.getCartItems().observe(getViewLifecycleOwner(), lst ->
                cartAdapter.setCartList(lst != null ? lst : new ArrayList<>())
        );
        cartVM.getSubtotal().observe(getViewLifecycleOwner(), sub ->
                binding.subtotalView.setText(formatPrice(sub))
        );
        cartVM.getTax().observe(getViewLifecycleOwner(), tax ->
                binding.taxView.setText(formatPrice(tax))
        );
        cartVM.getTotal().observe(getViewLifecycleOwner(), tot ->
                binding.totalView.setText(formatPrice(tot))
        );
        binding.deliveryFeeView.setText(formatPrice(5.0));
        cartVM.getDiscountAmount().observe(getViewLifecycleOwner(), discount ->
                binding.discountAmountView.setText(formatDiscount(discount))
        );
        cartVM.getVoucherError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
        cartVM.getAppliedVoucher().observe(getViewLifecycleOwner(), code -> {
            if (code != null) {
                Toast.makeText(requireContext(),
                        "Voucher " + code + " applied!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        cartVM.getVoucher().observe(getViewLifecycleOwner(), voucher -> {
            if (voucher != null) {
                binding.voucherBanner.setVisibility(View.VISIBLE);
                // Khi voucher thay đổi, cập nhật mã và tên
                binding.tvVoucherSelected.setText("Voucher: " + voucher.getCode() + "- saved " + voucher.getDescription());
            } else {
                binding.voucherBanner.setVisibility(View.GONE);
            }
        });

        cartVM.getDiscountAmount().observe(getViewLifecycleOwner(), discount -> {
            if (discount != null && discount > 0) {
                binding.tvDiscountAmount.setVisibility(View.VISIBLE);
                binding.tvDiscountAmount.setText(String.format("Saved: %.0f$", discount));
            } else {
                binding.tvDiscountAmount.setVisibility(View.GONE);
            }
        });

        cartVM.getLastCreatedOrderId().observe(getViewLifecycleOwner(), orderId -> {
            if ("card".equals(currentPaymentMethod) && orderId != null && pendingTotalAmount != null && pendingUserId != null) {
                pendingOrderIdForPayment = orderId;
                Log.d("CartFragment", "Received OrderId from ViewModel for CARD payment: " + pendingOrderIdForPayment);

                Intent intent = new Intent(requireContext(), PaymentActivity.class);
                intent.putExtra("totalAmount", pendingTotalAmount);
                intent.putExtra("orderId", pendingOrderIdForPayment);
                intent.putExtra("userId", pendingUserId);
                startActivityForResult(intent, CARD_PAYMENT_REQUEST_CODE);
                cartVM.resetLastCreatedOrderId();

                pendingTotalAmount = null;
                pendingOrderIdForPayment = null;
                pendingUserId = null;

            } else if (orderId != null) {
                Log.e("CartFragment", "Unexpected orderId received. currentPaymentMethod=" + currentPaymentMethod +
                        ", orderId=" + orderId + ", pendingTotalAmount=" + pendingTotalAmount +
                        ", pendingUserId=" + pendingUserId);
                Toast.makeText(requireContext(), "Internal error: Unable to retrieve payment information. The order is being canceled...", Toast.LENGTH_LONG).show();
                cartVM.deleteOrder(orderId);
                cartVM.resetLastCreatedOrderId();
                pendingTotalAmount = null;
                pendingOrderIdForPayment = null;
                pendingUserId = null;
                currentPaymentMethod = null;
            }
        });

        cartVM.getOrderPlaced().observe(getViewLifecycleOwner(), isOrderPlaced -> {
            if (isOrderPlaced != null && isOrderPlaced) {
                Toast.makeText(requireContext(), "Your order has been placed successfully!", Toast.LENGTH_SHORT).show();
                cartVM.resetOrderPlacedStatus();
                // **Tùy chọn: Xóa văn bản ghi chú sau khi đặt hàng**
                if (binding.notesEditText != null) {
                    binding.notesEditText.setText("");
                }
            }
        });

        cartVM.getCartCleared().observe(getViewLifecycleOwner(), isCleared -> {
            if (isCleared != null && isCleared) {
                Log.d("CartFragment", "CartViewModel reported cart cleared. Reloading local cart.");
                cartVM.resetCartClearedStatus();
                cartVM.loadCartFromFirestore();
                currentPaymentMethod = null;
            }
        });
    }

    private String formatPrice(double v) {
        return String.format("%,.0f $", v);
    }

    private String formatDiscount(double v) {
        return String.format("- %,.0f $", v);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            VoucherModel selectedVoucher = (VoucherModel) data.getSerializableExtra("selectedVoucher");
            if (selectedVoucher != null) {
                cartVM.applyVoucherObject(selectedVoucher);
            }
        }
        else if (requestCode == CARD_PAYMENT_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                Toast.makeText(requireContext(), "Card payment successful! Your order is being processed.", Toast.LENGTH_LONG).show();
                cartVM.clearCartInFirestoreAndLocal();
            } else {
                Toast.makeText(requireContext(), "Card payment was cancelled or failed. The order has been cancelled.", Toast.LENGTH_SHORT).show();
                if (data != null) {
                    String failedOrderId = data.getStringExtra("orderId");
                    if (failedOrderId != null) {
                        cartVM.deleteOrder(failedOrderId);
                        Log.d("CartFragment", "Deleted order " + failedOrderId + " due to payment cancellation/failure.");
                    } else {
                        Log.w("CartFragment", "Could not retrieve orderId to delete after payment cancellation/failure.");
                    }
                } else {
                    Log.w("CartFragment", "Data from PaymentActivity is null after cancellation/failure.");
                }
            }
            pendingTotalAmount = null;
            pendingOrderIdForPayment = null;
            pendingUserId = null;
            currentPaymentMethod = null;
        }
    }
}