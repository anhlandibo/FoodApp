package com.example.foodapp2025.ui.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.OrderModel;
import com.example.foodapp2025.viewmodel.OrderViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<OrderModel> orderModels = new ArrayList<>();
    private String userRole;
    private OrderViewModel orderViewModel;
    private OnOrderActionListener listener;
    private boolean isForRetrievedTab = false; // New field to indicate if this adapter is for the "Retrieved" tab

    public OrderAdapter(String userRole, OrderViewModel orderViewModel) {
        this.userRole = userRole;
        this.orderViewModel = orderViewModel;
    }

    /**
     * Constructor allowing to set the tab context.
     * @param userRole The role of the current user (e.g., "customer", "shipper").
     * @param orderViewModel ViewModel for order operations.
     * @param isForRetrievedTab True if this adapter instance is intended for a "Retrieved" orders tab/view.
     */
    public OrderAdapter(String userRole, OrderViewModel orderViewModel, boolean isForRetrievedTab) {
        this.userRole = userRole;
        this.orderViewModel = orderViewModel;
        this.isForRetrievedTab = isForRetrievedTab;
    }

    /**
     * Setter for the retrieved tab context, useful if the same adapter instance is reused.
     * @param forRetrievedTab True to set this adapter for a retrieved tab context, false otherwise.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setForRetrievedTab(boolean forRetrievedTab) {
        isForRetrievedTab = forRetrievedTab;
        notifyDataSetChanged(); // Refresh items to apply new display rules
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setOrderList(ArrayList<OrderModel> orderModels) {
        this.orderModels = orderModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderAdapter.OrderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        OrderModel orderModel = orderModels.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Set common order details
        holder.orderId.setText(orderModel.getId());
        holder.orderTime.setText(sdf.format(orderModel.getOrderedDate()));
        holder.orderStatus.setText(String.valueOf(orderModel.getStatus()));

        // Handle button visibility and actions based on user role
        if (userRole.equals("shipper")) {
            handleShipperView(holder, orderModel);
        } else {
            handleCustomerView(holder, orderModel);
        }

        // Set item click listener for navigation
        holder.itemView.setOnClickListener(v -> navigateToOrderDetail(v, orderModel));
    }

    /**
     * Handles UI elements and actions specific to the "shipper" role.
     */
    private void handleShipperView(OrderViewHolder holder, OrderModel orderModel) {
        holder.buttonReportOrder.setVisibility(View.GONE);
        holder.txtOrderReported.setVisibility(View.GONE);
        holder.buttonRetrieveOrder.setVisibility(View.GONE); // Shipper doesn't retrieve

        if ("delivering".equals(orderModel.getStatus())) {
            holder.btnConfirm.setVisibility(View.VISIBLE);
            holder.btnConfirm.setOnClickListener(v -> {
                orderViewModel.confirmOrderReceived(orderModel.getId())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                holder.btnConfirm.setVisibility(View.GONE);
                            } else {
                                Log.e("OrderAdapter", "Failed to confirm order: " + task.getException().getMessage());
                            }
                        });
            });
        } else {
            holder.btnConfirm.setVisibility(View.GONE);
            holder.btnConfirm.setOnClickListener(null); // Clear listener
        }
    }

    /**
     * Handles UI elements and actions specific to customer roles.
     */
    @SuppressLint("SetTextI18n")
    private void handleCustomerView(OrderViewHolder holder, OrderModel orderModel) {
        holder.btnConfirm.setVisibility(View.GONE); // Customer doesn't confirm receipt

        resetCustomerButtonStates(holder); // Reset all customer related buttons first

        String currentStatus = orderModel.getStatus();

        // Handle Report button visibility
        // Report button is visible only if order is "completed" and not yet reported
        if ("completed".equals(currentStatus) && orderModel.getReportStatus() == 0) {
            holder.buttonReportOrder.setVisibility(View.VISIBLE);
            holder.buttonReportOrder.setOnClickListener(v -> showReportDialog(holder.itemView, orderModel));
        } else {
            holder.buttonReportOrder.setVisibility(View.GONE);
            holder.buttonReportOrder.setOnClickListener(null);
        }

        // Handle Retrieve button visibility and reported/retrieved status text
        // Display "retrieved" text ONLY if the order is retrieved AND this adapter is for the retrieved tab
        if (orderModel.getRetrieveStatus() == 1) {
            if (isForRetrievedTab) {
                holder.txtOrderReported.setText("retrieved");
                holder.txtOrderReported.setVisibility(View.VISIBLE);
            } else {
                // If it's retrieved but not on the retrieved tab, ensure text is hidden
                holder.txtOrderReported.setVisibility(View.GONE);
                holder.txtOrderReported.setText("");
            }
        }
        // Display "order reported" text if the order has been reported (and not retrieved, handled by above if)
        else if (orderModel.getReportStatus() != 0) {
            holder.txtOrderReported.setText("order reported");
            holder.txtOrderReported.setVisibility(View.VISIBLE);
        }


        // Show 'retrieve' button for pending, cancelled, or returned orders
        if ("pending".equals(currentStatus) || "cancelled".equals(currentStatus) || "returned".equals(currentStatus)) {
            holder.buttonRetrieveOrder.setVisibility(View.VISIBLE);
            holder.buttonRetrieveOrder.setPaintFlags(holder.buttonRetrieveOrder.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.buttonRetrieveOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderRetrieveRequested(orderModel, holder.itemView);
                }
            });
        } else {
            holder.buttonRetrieveOrder.setVisibility(View.GONE);
            holder.buttonRetrieveOrder.setOnClickListener(null); // Clear listener
            holder.buttonRetrieveOrder.setPaintFlags(holder.buttonRetrieveOrder.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG)); // Remove underline
        }
    }

    /**
     * Resets the visibility and text of customer-specific UI elements.
     * This helps in preventing view recycling issues where previous states might persist.
     */
    private void resetCustomerButtonStates(OrderViewHolder holder) {
        holder.buttonReportOrder.setVisibility(View.GONE);
        holder.buttonReportOrder.setOnClickListener(null);

        holder.buttonRetrieveOrder.setVisibility(View.GONE);
        holder.buttonRetrieveOrder.setOnClickListener(null);
        holder.buttonRetrieveOrder.setPaintFlags(holder.buttonRetrieveOrder.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        holder.txtOrderReported.setVisibility(View.GONE);
        holder.txtOrderReported.setText("");
    }

    /**
     * Navigates to the Order Detail Fragment with the selected order model.
     */
    private void navigateToOrderDetail(View v, OrderModel selectedOrder) {
        NavController navController = Navigation.findNavController(v);
        Bundle bundle = new Bundle();
        bundle.putSerializable("order", selectedOrder); // OrderModel must implement Serializable
        navController.navigate(R.id.orderDetailFragment, bundle);
    }

    @Override
    public int getItemCount() {
        return orderModels.size();
    }

    /**
     * ViewHolder class for individual order items.
     */
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderTime, buttonReportOrder, txtOrderReported, buttonRetrieveOrder;
        Button btnConfirm;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);
            buttonReportOrder = itemView.findViewById(R.id.btn_report_order);
            buttonReportOrder.setPaintFlags(buttonReportOrder.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtOrderReported = itemView.findViewById(R.id.txt_order_reported);
            btnConfirm = itemView.findViewById(R.id.btn_confirm_order);
            buttonRetrieveOrder = itemView.findViewById(R.id.btn_retrieve_order);
        }
    }

    /**
     * Interface for communicating actions back to the fragment/activity.
     */
    public interface OnOrderActionListener {
        void onReportSubmitted(OrderModel orderModel, View itemView); // Add View parameter
        void onOrderRetrieveRequested(OrderModel orderModel, View itemView);
        void onItemClicked(OrderModel orderModel); // This is not currently used but kept for completeness
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    /**
     * Displays a dialog for reporting an order issue.
     */
    private void showReportDialog(View view, OrderModel orderModel) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_report_order, null);

        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Get references to dialog elements
        RadioGroup radioGroupIssues = dialogView.findViewById(R.id.radioGroupIssues);
        EditText editTextDetails = dialogView.findViewById(R.id.editTextDetails);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSendReport = dialogView.findViewById(R.id.btnSendReport);

        // Handle Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Handle Send Report button
        btnSendReport.setOnClickListener(v -> {
            int selectedRadioId = radioGroupIssues.getCheckedRadioButtonId();

            if (selectedRadioId == -1) {
                // No radio button selected - show error (e.g., Toast)
                return;
            }

            int reportStatus = 0; // Default or 'no report'
            if (selectedRadioId == R.id.radioNotDelivered) {
                reportStatus = 1; // not receiving
            } else if (selectedRadioId == R.id.radioFaulty) {
                reportStatus = 2; // quality issue
            } else if (selectedRadioId == R.id.radioWrongItem) {
                reportStatus = 3; // wrong food
            }

            String additionalInfo = editTextDetails.getText().toString().trim();

            orderModel.setReportStatus(reportStatus);
            orderModel.setReportAdditionalInfo(additionalInfo);

            if (listener != null) {
                listener.onReportSubmitted(orderModel, view);
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}