package com.example.foodapp2025.ui.fragment;

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

import com.example.foodapp2025.databinding.FragmentCartBinding;
import com.example.foodapp2025.ui.adapter.CartAdapter;
import com.example.foodapp2025.viewmodel.CartViewModel;
import com.example.foodapp2025.data.model.CartModel;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupViewModel() {
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(
                new ArrayList<>(),
                cartItem -> cartViewModel.calculatePriceDetails(cartItem),
                (cartItem, position) -> {
                    cartViewModel.removeItem(cartItem);
                    Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                }
        );

        binding.cartView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartView.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        binding.voucherButton.setOnClickListener(v -> applyVoucher());
        binding.button2.setOnClickListener(v -> {
            boolean orderPlaced = cartViewModel.placeOrder();
            if (!orderPlaced) {
                Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Order placed!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::updateCartList);
        cartViewModel.getSubtotal().observe(getViewLifecycleOwner(), subtotal -> binding.subtotalView.setText(formatPrice(subtotal)));
        cartViewModel.getTax().observe(getViewLifecycleOwner(), tax -> binding.taxView.setText(formatPrice(tax)));
        cartViewModel.getTotal().observe(getViewLifecycleOwner(), total -> binding.totalView.setText(formatPrice(total)));
    }

    private void updateCartList(List<CartModel> cartList) {
        cartAdapter.setCartList(cartList != null ? cartList : new ArrayList<>());
    }

    private void applyVoucher() {
        String code = binding.voucherTxt.getText().toString().trim();
        if (!code.isEmpty()) {
            cartViewModel.applyVoucher(code);
            Toast.makeText(requireContext(), "Voucher applied!", Toast.LENGTH_SHORT).show();
            binding.voucherTxt.setText("");
        } else {
            binding.voucherTxt.setError("Enter a valid voucher code");
        }
    }

    private String formatPrice(double price) {
        return String.format("%.0f VND", price);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
