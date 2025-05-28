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

import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.databinding.FragmentCartBinding;
import com.example.foodapp2025.ui.activity.VoucherSelectionActivity;
import com.example.foodapp2025.ui.adapter.CartAdapter;
import com.example.foodapp2025.viewmodel.CartViewModel;
import com.example.foodapp2025.data.model.CartModel;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartViewModel cartVM;
    private CartAdapter cartAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle s) {
        binding = FragmentCartBinding.inflate(inflater, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        cartVM = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        setupRecycler();
        setupObservers();
        setupListeners();
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
            boolean ok = cartVM.placeOrder();
            Toast.makeText(requireContext(),
                    ok ? "Order placed!" : "Your cart is empty.",
                    Toast.LENGTH_SHORT).show();
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
                cartVM.applyVoucherObject(selectedVoucher); // Bạn cần tạo phương thức này trong CartViewModel
            }
        }
    }

}
