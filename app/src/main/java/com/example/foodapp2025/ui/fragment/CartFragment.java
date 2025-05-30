//package com.example.foodapp2025.ui.fragment;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.foodapp2025.R;
//import com.example.foodapp2025.data.model.VoucherModel;
//import com.example.foodapp2025.databinding.FragmentCartBinding;
//import com.example.foodapp2025.ui.activity.PaymentActivity;
//import com.example.foodapp2025.ui.activity.VoucherSelectionActivity;
//import com.example.foodapp2025.ui.adapter.CartAdapter;
//import com.example.foodapp2025.viewmodel.CartViewModel;
//import com.example.foodapp2025.data.model.CartModel;
//
//import java.util.ArrayList;
//
//public class CartFragment extends Fragment {
//    private FragmentCartBinding binding;
//    private CartViewModel cartVM;
//    private CartAdapter cartAdapter;
//    private static final int CARD_PAYMENT_REQUEST_CODE = 1002;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle s) {
//        binding = FragmentCartBinding.inflate(inflater, parent, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
//        super.onViewCreated(v, s);
//        cartVM = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
//        setupRecycler();
//        setupObservers();
//        setupListeners();
//        binding.paymentMethodRadioGroup.check(R.id.radioCod);
//    }
//
//    private void setupRecycler() {
//        cartAdapter = new CartAdapter(
//                new ArrayList<>(),
//                ci -> cartVM.updateQuantity(ci, ci.getQuantity()), // recalc giá khi sửa số lượng
//                (ci, pos) -> {
//                    cartVM.removeItem(ci);
//                    Toast.makeText(getContext(),"Removed", Toast.LENGTH_SHORT).show();
//                }
//        );
//        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.cartView.setAdapter(cartAdapter);
//    }
//
//    private void setupListeners() {
//        binding.voucherButton.setOnClickListener(btn -> {
//            Intent intent = new Intent(requireContext(), VoucherSelectionActivity.class);
//            startActivityForResult(intent, 1001);
//            //code = binding.voucherTxt.getText().toString().trim();
//            //cartVM.applyVoucher(code);
//            //binding.voucherTxt.setText("");
//        });
//        binding.button2.setOnClickListener(btn -> {
//            int selectedPaymentMethodId = binding.paymentMethodRadioGroup.getCheckedRadioButtonId();
//
//            if (selectedPaymentMethodId == R.id.radioCod) {
//                boolean ok = cartVM.placeOrder();
//                Toast.makeText(requireContext(),
//                        ok ? "Đơn hàng đã được đặt (COD)!" : "Giỏ hàng của bạn đang trống.",
//                        Toast.LENGTH_SHORT).show();
//                if (ok) {
//                }
//            } else if (selectedPaymentMethodId == R.id.radioCard) {
//                if (cartVM.getCartItems().getValue() == null || cartVM.getCartItems().getValue().isEmpty()) {
//                    Toast.makeText(requireContext(), "Giỏ hàng của bạn đang trống.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                Intent intent = new Intent(requireContext(), PaymentActivity.class);
//                startActivityForResult(intent, CARD_PAYMENT_REQUEST_CODE);
//            } else {
//                Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void setupObservers() {
//        cartVM.getCartItems().observe(getViewLifecycleOwner(), lst ->
//                cartAdapter.setCartList(lst != null ? lst : new ArrayList<>())
//        );
//        cartVM.getSubtotal().observe(getViewLifecycleOwner(), sub ->
//                binding.subtotalView.setText(formatPrice(sub))
//        );
//        cartVM.getTax().observe(getViewLifecycleOwner(), tax ->
//                binding.taxView.setText(formatPrice(tax))
//        );
//        cartVM.getTotal().observe(getViewLifecycleOwner(), tot ->
//                binding.totalView.setText(formatPrice(tot))
//        );
//        cartVM.getVoucherError().observe(getViewLifecycleOwner(), err -> {
//            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
//        });
//        cartVM.getAppliedVoucher().observe(getViewLifecycleOwner(), code -> {
//            if (code != null) {
//                Toast.makeText(requireContext(),
//                        "Voucher " + code + " applied!",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private String formatPrice(double v) {
//        return String.format("%,.0f VND", v);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
//            VoucherModel selectedVoucher = (VoucherModel) data.getSerializableExtra("selectedVoucher");
//            if (selectedVoucher != null) {
//                cartVM.applyVoucherObject(selectedVoucher);
//            }
//        }
//        else if (requestCode == CARD_PAYMENT_REQUEST_CODE) {
//        if (resultCode == getActivity().RESULT_OK) {
//            boolean ok = cartVM.placeOrder();
//            Toast.makeText(requireContext(),
//                    ok ? "Đơn hàng đã được đặt (Thẻ)!" : "Có lỗi khi đặt hàng sau thanh toán thẻ.",
//                    Toast.LENGTH_SHORT).show();
//            if (ok) {
//
//            }
//        } else {
//            Toast.makeText(requireContext(), "Thanh toán thẻ bị hủy hoặc thất bại.", Toast.LENGTH_SHORT).show();
//        }
//    }
//    }
//
//}

package com.example.foodapp2025.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.foodapp2025.data.model.CartModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartViewModel cartVM;
    private CartAdapter cartAdapter;

    private FirebaseAuth mAuth;
    private static final int CARD_PAYMENT_REQUEST_CODE = 1002;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle s) {
        binding = FragmentCartBinding.inflate(inflater, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        cartVM = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        setupRecycler();
        setupObservers();
        setupListeners();
        binding.paymentMethodRadioGroup.check(R.id.radioCod);
    }

    private void setupRecycler() {
        cartAdapter = new CartAdapter(
                new ArrayList<>(),
                ci -> cartVM.updateQuantity(ci, ci.getQuantity()), // recalc giá khi sửa số lượng
                (ci, pos) -> {
                    cartVM.removeItem(ci);
                    Toast.makeText(getContext(),"Removed", Toast.LENGTH_SHORT).show();
                }
        );
        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartView.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        binding.voucherButton.setOnClickListener(btn -> {
            Intent intent = new Intent(requireContext(), VoucherSelectionActivity.class);
            startActivityForResult(intent, 1001);
            //code = binding.voucherTxt.getText().toString().trim();
            //cartVM.applyVoucher(code);
            //binding.voucherTxt.setText("");
        });
        binding.button2.setOnClickListener(btn -> {
            int selectedPaymentMethodId = binding.paymentMethodRadioGroup.getCheckedRadioButtonId();

            if (selectedPaymentMethodId == R.id.radioCod) {
                boolean ok = cartVM.placeOrder();
                Toast.makeText(requireContext(),
                        ok ? "Đơn hàng đã được đặt (COD)!" : "Giỏ hàng của bạn đang trống.",
                        Toast.LENGTH_SHORT).show();
                if (ok) {
                }
            } else if (selectedPaymentMethodId == R.id.radioCard) {
                if (cartVM.getCartItems().getValue() == null || cartVM.getCartItems().getValue().isEmpty()) {
                    Toast.makeText(requireContext(), "Giỏ hàng của bạn đang trống.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(requireContext(), PaymentActivity.class);
                if (cartVM.getTotal().getValue() != null) {
                    intent.putExtra("totalAmount", cartVM.getTotal().getValue());
                } else {
                    intent.putExtra("totalAmount", 0.0);
                }
                startActivityForResult(intent, CARD_PAYMENT_REQUEST_CODE);
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
            }
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
    }

    private String formatPrice(double v) {
        return String.format("%,.0f VND", v);
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
                boolean ok = cartVM.placeOrder();
                Toast.makeText(requireContext(),
                        ok ? "Đơn hàng đã được đặt (Thẻ)!" : "Có lỗi khi đặt hàng sau thanh toán thẻ.",
                        Toast.LENGTH_SHORT).show();
                if (ok) {

                }
            } else {
                Toast.makeText(requireContext(), "Thanh toán thẻ bị hủy hoặc thất bại.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

