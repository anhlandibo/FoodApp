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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<OrderModel> orderModels = new ArrayList<>();
    public OrderAdapter(){}
    public OrderAdapter(ArrayList<OrderModel> orderModels){
        this.orderModels = orderModels;
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
        holder.orderId.setText(orderModel.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.orderTime.setText(sdf.format(orderModel.getOrderedDate()));
        holder.orderStatus.setText(String.valueOf(orderModel.getStatus()));

        TextView btnReport = holder.buttonReportOrder;
        TextView txtOrderReported = holder.txtOrderReported;
        if ("completed".equals(orderModel.getStatus()) && orderModel.getReportStatus() == 0){
            Log.d("OrderAdapter", "Status is 'completed'. Checking if button is null for ID: " + orderModel.getId() + " -> " + (btnReport == null)); // Kiểm tra lại null lần cuối trước khi dùng
            if (btnReport != null){
                Log.d("OrderAdapter", "Status is 'completed'. About to set button VISIBLE for ID: " + orderModel.getId()); // <<< Log NGAY TRƯỚC setVisibility
                btnReport.setVisibility(View.VISIBLE);
                txtOrderReported.setVisibility(View.GONE);
                Log.d("OrderAdapter", "Called setVisibility(VISIBLE) for ID: " + orderModel.getId()); // <<< Log NGAY SAU setVisibility
                btnReport.setOnClickListener(v -> {
                    showReportDialog(holder.itemView, orderModel); // Pass the full orderModel instead of just ID
                });
            }
            else {
                Log.e("OrderAdapter", "Confirm button is NULL despite successful find in ViewHolder for ID: " + orderModel.getId());
            }
        }
        else{
            if (btnReport != null) {
                btnReport.setVisibility(View.GONE);
                btnReport.setOnClickListener(null);
            }
            if (orderModel.getReportStatus() != 0) {
                txtOrderReported.setVisibility(View.VISIBLE);
            }
            else {
                txtOrderReported.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            OrderModel selectedOrder = orderModels.get(position);
            NavController navController = Navigation.findNavController(v);
            Bundle bundle = new Bundle();
            bundle.putSerializable("order", selectedOrder); // assuming OrderModel implements Serializable
            navController.navigate(R.id.orderDetailFragment, bundle); // make sure this ID matches your nav_graph
        });

    }

    @Override
    public int getItemCount() {
        return orderModels.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderTime, buttonReportOrder, txtOrderReported;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);
            buttonReportOrder = itemView.findViewById(R.id.btn_report_order);
            buttonReportOrder.setPaintFlags(buttonReportOrder.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtOrderReported = itemView.findViewById(R.id.txt_order_reported);
        }
    }

    public interface OnOrderActionListener {
        void onReportSubmitted(OrderModel orderModel, View itemView); // Add View parameter

    }
    private OnOrderActionListener listener;

    public void setOnOrderActionListener(OnOrderActionListener listener){
        this.listener = listener;
    }
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
                // No radio button selected - show error
                // You might want to show a Toast or highlight the radio group
                return;
            }

            // Determine report status based on selected radio button
            int reportStatus = 0;
            if (selectedRadioId == R.id.radioNotDelivered) {
                reportStatus = 1; // not receiving
            } else if (selectedRadioId == R.id.radioFaulty) {
                reportStatus = 2; // quality issue
            } else if (selectedRadioId == R.id.radioWrongItem) {
                reportStatus = 3; // wrong food
            }

            // Get additional details
            String additionalInfo = editTextDetails.getText().toString().trim();

            // Update the order model
            orderModel.setReportStatus(reportStatus);
            orderModel.setReportAdditionalInfo(additionalInfo);

            // Call interface method to handle Firestore update
            if (listener != null) {
                listener.onReportSubmitted(orderModel, view);
            }

            dialog.dismiss();
        });

        dialog.show();
    }}
