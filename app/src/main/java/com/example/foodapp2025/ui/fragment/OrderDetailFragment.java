package com.example.foodapp2025.ui.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.databinding.FragmentOrderDetailBinding;
import com.example.foodapp2025.ui.adapter.OrderDetailAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class OrderDetailFragment extends Fragment {
    private boolean isCollapsed = false;
    private FragmentOrderDetailBinding binding;
    private OrderDetailAdapter orderDetailAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderDetailAdapter = new OrderDetailAdapter();
        binding.orderDetailRecyclerView.setAdapter(orderDetailAdapter);
        binding.orderDetailRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        assert getArguments() != null;

        OrderModel order = (OrderModel) getArguments().getSerializable("order");

        ArrayList<Map<String, Object>> items = order.getItems();
        for (Map<String, Object> item : items) {
            String imageUrl = (String) item.get("imageUrl");
            String name = (String) item.get("name");
            Double price = (Double) item.get("price");
            Long quantity = (Long) item.get("quantity");
            orderDetailAdapter.addItem(new CartModel(imageUrl, name, price, quantity));
        }

        // Basic order info (already bound)
        binding.orderId.setText(order.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        binding.orderedDate.setText(sdf.format(order.getOrderedDate()));

        // Payment summary - bind all missing fields
        binding.orderTax.setText(order.getTax() + " $");
        binding.orderSubtotal.setText(order.getSubtotal() + " $");

        // Handle discount amount
        binding.orderDiscount.setText("-" + order.getDiscountAmount() + " $");
        binding.orderDeliveryFee.setText(formatPrice(5.0));
        if (order.getPaymentMethod().equals("cod")) {
            binding.orderPaymentMethod.setText("Cash on delivery");
        } else if (order.getPaymentMethod().equals("card")) {
            binding.orderPaymentMethod.setText("Online payment");
        }
        binding.orderTotal.setText(order.getTotal() + " $");
        if (order.getPaymentStatus().equals("paid")) {
            binding.orderPaid.setText(order.getTotal() + " $");
            binding.orderRemaining.setText("0 $");
        } else {
            binding.orderPaid.setText("0 $");
            binding.orderRemaining.setText(order.getTotal() + " $");
        }
        // Display voucher codes
        displayVoucherDetails(order);

        binding.backArrowOrderDetail.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.collapseButton.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(binding.getRoot());
            if (isCollapsed) {
                binding.paymentSummaryContainer.setVisibility(View.VISIBLE);
                binding.collapseButton.setText("Collapse"); // Collapse
            } else {
                binding.paymentSummaryContainer.setVisibility(View.GONE);
                binding.collapseButton.setText("Expand"); // Expand
            }
            isCollapsed = !isCollapsed;
        });
    }

    private String formatPrice(double v) {
        return String.format("%,.0f $", v);
    }


    private void displayVoucherDetails(OrderModel order) {
        // Handle voucher codes display
        LinearLayout voucherCodesContainer = binding.voucherCodesContainer;

        // Clear any existing voucher views
        voucherCodesContainer.removeAllViews();

        // Check if there's any voucher applied
        String voucherCode = order.getVoucherCode();

        if (voucherCode != null && !voucherCode.isEmpty()) {
            // Show the container
            voucherCodesContainer.setVisibility(View.VISIBLE);

            // Add the voucher code to the list
            addVoucherCodeView(voucherCodesContainer, voucherCode);

            // If you have multiple vouchers in the future, you can loop through them here

        } else {
            // Hide voucher section if no voucher applied
            voucherCodesContainer.setVisibility(View.GONE);
        }
    }

    private void addVoucherCodeView(LinearLayout container, String voucherCode) {
        // Create a new LinearLayout for this voucher
        LinearLayout voucherRow = new LinearLayout(getContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dpToPx(4)); // 4dp bottom margin
        voucherRow.setLayoutParams(rowParams);
        voucherRow.setOrientation(LinearLayout.HORIZONTAL);
        voucherRow.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        // Create TextView for "Voucher's code:"
        TextView labelTextView = new TextView(getContext());
        labelTextView.setText("Voucher's code: ");
        labelTextView.setTextColor(getResources().getColor(android.R.color.black));
        labelTextView.setTextSize(11);

        // Create TextView for the actual code
        TextView codeTextView = new TextView(getContext());
        codeTextView.setText(voucherCode);
        codeTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        codeTextView.setTextSize(11);
        codeTextView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Add TextViews to the row
        voucherRow.addView(labelTextView);
        voucherRow.addView(codeTextView);

        // Add the row to the container
        container.addView(voucherRow);
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}




