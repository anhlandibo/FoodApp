package com.example.foodapp2025.ui.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            Long price = (Long) item.get("price");
            Long quantity = (Long) item.get("quantity");
            orderDetailAdapter.addItem(new CartModel(imageUrl, name, price, quantity));
        }
        binding.orderId.setText(order.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        binding.orderedDate.setText(sdf.format(order.getOrderedDate()));
        binding.orderTotal.setText(String.valueOf(order.getTotal())+ " VND");
        binding.orderPaid.setText(String.valueOf(order.getTotal())+ " VND");
        binding.backArrowOrderDetail.setOnClickListener( v-> {
            requireActivity().onBackPressed();
        });

        binding.collapseButton.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(binding.getRoot());
            if (isCollapsed) {
                binding.paymentSummaryContainer.setVisibility(View.VISIBLE);
                binding.collapseButton.setText("Collapse"); // Collapse
                // Optional: set icon if using compoundDrawables
                // collapseButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_up, 0);
            } else {
                binding.paymentSummaryContainer.setVisibility(View.GONE);
                binding.collapseButton.setText("Expand"); // Expand
                // collapseButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_down, 0);
            }
            isCollapsed = !isCollapsed;
        });

    }
}




