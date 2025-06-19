package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.databinding.FragmentHistoryBinding;
import com.example.foodapp2025.ui.activity.MainActivity;
import com.example.foodapp2025.ui.adapter.OrderAdapter;
import com.example.foodapp2025.viewmodel.OrderViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements OrderAdapter.OnOrderActionListener {
    private FragmentHistoryBinding binding;
    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private ArrayList<OrderModel> allOrders = new ArrayList<>();
    private String userRole; // This is the variable we need to make sure is not null

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Define a constant for the user role argument key (good practice)
    private static final String ARG_USER_ROLE = "user_role";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        // While NavigationUI might not use this, it's good practice for direct instantiation
        // args.putString(ARG_USER_ROLE, userRole); // No userRole here, as newInstance itself doesn't know it
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            // First, try to get userRole from this fragment's arguments
            userRole = getArguments().getString(ARG_USER_ROLE); // Use the constant
        }

        // If userRole is still null (e.g., from NavigationUI), try to get it from the parent activity (MainActivity)
        if (userRole == null && getActivity() instanceof MainActivity) {
            userRole = ((MainActivity) getActivity()).getUserRole();
            Log.d("HistoryFragment", "User role fetched from MainActivity: " + userRole);
        }

        // Fallback to default if userRole is still null after checking arguments and MainActivity
        if (userRole == null) {
            userRole = "customer";
            Log.w("HistoryFragment", "User role not found in arguments or MainActivity, defaulting to customer.");
        }
        // Log the final userRole after all attempts
        Log.d("HistoryFragment", "Final resolved user role: " + userRole);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        // Defensive check: Ensure userRole is not null before using it to create the adapter.
        // This should ideally be caught in onCreate, but it's an extra layer of safety.
        if (userRole == null) {
            userRole = "customer";
            Log.e("HistoryFragment", "userRole was unexpectedly null in onViewCreated, defaulting to customer.");
        }

        orderAdapter = new OrderAdapter(userRole, orderViewModel);
        orderAdapter.setOnOrderActionListener(this);
        binding.orderRecyclerView.setAdapter(orderAdapter);
        binding.orderRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));


        // Show loading before data fetch
        binding.orderHistoryProgressBar.setVisibility(View.VISIBLE);
        // This is the line where the NullPointerException happened before
        if (userRole.equalsIgnoreCase("shipper")) {
            orderViewModel.getCurrentShippersOrders().observe(getViewLifecycleOwner(), orderList -> {
                allOrders.clear();
                if (orderList != null) {
                    allOrders.addAll(orderList);
                    Log.d("HistoryFragment", "Received " + orderList.size() + " orders");
                }
                orderAdapter.setOrderList(allOrders);

                // Hide loading after data fetch completes
                binding.orderHistoryProgressBar.setVisibility(View.GONE);
            });
        } else {
            orderViewModel.getCurrentUsersOrders().observe(getViewLifecycleOwner(), orderList -> {
                allOrders.clear();
                if (orderList != null) {
                    allOrders.addAll(orderList);
                    Log.d("HistoryFragment", "Received " + orderList.size() + " orders");
                }
                orderAdapter.setOrderList(allOrders);

                // Hide loading after data fetch completes
                binding.orderHistoryProgressBar.setVisibility(View.GONE);
            });
        }
        TabLayout tabs = binding.tabs;
        tabs.addTab(tabs.newTab().setText("All"));
        tabs.addTab(tabs.newTab().setText("Processing"));
        tabs.addTab(tabs.newTab().setText("Delivered"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ArrayList<OrderModel> filtered;
                // Add a null check for allOrders here too, though it should be initialized
                List<OrderModel> currentOrders = allOrders != null ? allOrders : new ArrayList<>();

                switch (tab.getPosition()) {
                    case 1: // Processing (Pending & Delivering)
                        filtered = filterByStatus(currentOrders, "delivering");
                        filtered.addAll(filterByStatus(currentOrders, "pending"));
                        break;
                    case 2: // Delivered (Completed)
                        filtered = filterByStatus(currentOrders, "completed");
                        break;
                    default: // 0: All
                        filtered = new ArrayList<>(currentOrders); // Create a new list to avoid modifying allOrders directly
                        break;
                }
                orderAdapter.setOrderList(filtered);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { /* no-op */ }
            @Override public void onTabReselected(TabLayout.Tab tab) {
                binding.orderRecyclerView.scrollToPosition(0);
            }
        });
    }


    // Helper to filter orders by status
    private ArrayList<OrderModel> filterByStatus(List<OrderModel> source, String status) {
        ArrayList<OrderModel> result = new ArrayList<>();
        for (OrderModel o : source) {
            if (status.equalsIgnoreCase(o.getStatus())) {
                result.add(o);
            }
        }
        return result;
    }

    @Override
    public void onReportSubmitted(OrderModel orderModel, View itemView) {
        if (orderViewModel != null && getContext() != null) {
            Toast.makeText(getContext(), "Reporting order...", Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "Please initiate a chat with us so we can confirm the issue and compensate fairly.", Toast.LENGTH_SHORT).show();
            orderViewModel.reportOrder(orderModel, itemView);
        } else {
            Log.e("HistoryFragment", "ViewModel or Context is null, cannot report order.");
        }
    }
}