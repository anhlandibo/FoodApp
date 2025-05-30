package com.example.foodapp2025.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private CartViewModel cartVM;
    private CartAdapter cartAdapter;

    // ActivityResultLauncher thay thế onActivityResult
    private final ActivityResultLauncher<Intent> voucherLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            VoucherModel selectedVoucher = (VoucherModel) result.getData().getSerializableExtra("selectedVoucher");
                            if (selectedVoucher != null) {
                                cartVM.applyVoucherObject(selectedVoucher);
                                //showVoucherBottomSheet(selectedVoucher);
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
        cartVM = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        setupRecycler();
        setupObservers();
        setupListeners();
    }

    private void setupRecycler() {
        cartAdapter = new CartAdapter(
                new ArrayList<>(),
                ci -> cartVM.updateQuantity(ci, ci.getQuantity()), // cập nhật số lượng
                (ci, pos) -> {
                    cartVM.removeItem(ci);
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
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
            boolean ok = cartVM.placeOrder();
            binding.voucherBanner.setVisibility(View.GONE);

            Toast.makeText(requireContext(),
                    ok ? "Order placed!" : "Your cart is empty.",
                    Toast.LENGTH_SHORT).show();
            cartVM.applyVoucherObject(null);
        });

        binding.btnRemoveVoucher.setOnClickListener(v -> {
            cartVM.applyVoucherObject(null);
            Toast.makeText(requireContext(), "Voucher đã bị hủy", Toast.LENGTH_SHORT).show();
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

        // Hiện phí vận chuyển cố định $5
        binding.deliveryFeeView.setText(formatPrice(5));

        cartVM.getTotal().observe(getViewLifecycleOwner(), tot ->
                binding.totalView.setText(formatPrice(tot))
        );

        cartVM.getVoucherError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
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
                binding.tvDiscountAmount.setText(String.format("Giảm: %.0f$", discount));
            } else {
                binding.tvDiscountAmount.setVisibility(View.GONE);
            }
        });

    }

    private String formatPrice(double v) {
        return String.format("%,.0f $", v);
    }

//    private void showVoucherBottomSheet(VoucherModel voucher) {
//        VoucherBottomSheetDialogFragment voucherSheet = VoucherBottomSheetDialogFragment.newInstance(voucher);
//        voucherSheet.setOnVoucherCancelledListener(() -> {
//            cartVM.applyVoucherObject(null);
//            Toast.makeText(requireContext(), "Voucher đã bị hủy", Toast.LENGTH_SHORT).show();
//        });
//        voucherSheet.show(getParentFragmentManager(), "VoucherBottomSheet");
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
