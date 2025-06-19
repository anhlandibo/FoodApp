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
import java.util.Date; // Make sure to import java.util.Date if OrderModel.getOrderedDate() returns Date

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private ArrayList<OrderModel> orderModels = new ArrayList<>();
    private OnOrderActionListener listener; // Declare listener here

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
        // Handle potential null date
        if (orderModel.getOrderedDate() != null) {
            holder.orderTime.setText(sdf.format(orderModel.getOrderedDate()));
        } else {
            holder.orderTime.setText("N/A"); // Default text if date is null
        }
        holder.orderStatus.setText(String.valueOf(orderModel.getStatus()));

        // --- DECLARE AND INITIALIZE VIEWS ---
        TextView btnRetrieve = holder.buttonRetrieveOrder;
        TextView btnReport = holder.buttonReportOrder;
        TextView txtOrderStatusInfo = holder.txtOrderStatusInfo; // Use the consolidated TextView

        // 1. Initialize all relevant views in the FrameLayout to GONE and clear listeners
        if (btnReport != null) {
            btnReport.setVisibility(View.GONE);
            btnReport.setOnClickListener(null); // Clear click listener
            btnReport.setPaintFlags(btnReport.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG)); // Remove underline
        }
        if (btnRetrieve != null) {
            btnRetrieve.setVisibility(View.GONE);
            btnRetrieve.setOnClickListener(null); // Clear click listener
            btnRetrieve.setPaintFlags(btnRetrieve.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG)); // Remove underline
        }
        if (txtOrderStatusInfo != null) {
            txtOrderStatusInfo.setVisibility(View.GONE);
            txtOrderStatusInfo.setText(""); // Clear text content
        }

        // --- Apply visibility logic for the FrameLayout contents ---
        // Prioritize showing 'retrieved' status text if order has been retrieved
        if (orderModel.getRetrieveStatus() == 1) { // Assuming getRetrieveStatus() exists in OrderModel
            if (txtOrderStatusInfo != null) {
                txtOrderStatusInfo.setText("retrieved");
                txtOrderStatusInfo.setVisibility(View.VISIBLE);
            }
        }
        // Else, if order has been reported (and not retrieved)
        else if (orderModel.getReportStatus() != 0) { // Assuming getReportStatus() exists in OrderModel
            if (txtOrderStatusInfo != null) {
                txtOrderStatusInfo.setText("order reported");
                txtOrderStatusInfo.setVisibility(View.VISIBLE);
            }
        }
        // Else (order is neither reported nor retrieved), show action buttons based on current status
        else {
            String currentStatus = orderModel.getStatus();
            if ("completed".equals(currentStatus)) { // Show 'report' button for completed orders
                if (btnReport != null) {
                    btnReport.setVisibility(View.VISIBLE);
                    btnReport.setPaintFlags(btnReport.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // Apply underline
                    btnReport.setOnClickListener(v -> {
                        showReportDialog(holder.itemView, orderModel);
                    });
                }
            } else if ("pending".equals(currentStatus) || "cancelled".equals(currentStatus) || "returned".equals(currentStatus)) {
                // Show 'retrieve' button for pending, cancelled, or returned orders
                if (btnRetrieve != null) {
                    btnRetrieve.setVisibility(View.VISIBLE);
                    btnRetrieve.setPaintFlags(btnRetrieve.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // Apply underline
                    btnRetrieve.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onOrderRetrieveRequested(orderModel, holder.itemView);
                        }
                    });
                }
            }
        }

        // --- Handle general item click ---
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
        TextView orderId, orderStatus, orderTime;
        TextView buttonReportOrder;
        TextView buttonRetrieveOrder;
        TextView txtOrderStatusInfo; // Consolidated TextView for status info

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);

            buttonReportOrder = itemView.findViewById(R.id.btn_report_order);
            buttonRetrieveOrder = itemView.findViewById(R.id.btn_retrieve_order);
            txtOrderStatusInfo = itemView.findViewById(R.id.txt_order_status_info); // Initialize the consolidated TextView
        }
    }

    // --- Interface for actions ---
    public interface OnOrderActionListener {
        void onReportSubmitted(OrderModel orderModel, View itemView);
        void onOrderRetrieveRequested(OrderModel orderModel, View itemView);
        void onItemClicked(OrderModel orderModel);
    }

    public void setOnOrderActionListener(OnOrderActionListener listener){
        this.listener = listener;
    }

    // --- Report Dialog Implementation (remains unchanged) ---
    private void showReportDialog(View view, OrderModel orderModel) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_report_order, null);

        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        RadioGroup radioGroupIssues = dialogView.findViewById(R.id.radioGroupIssues);
        EditText editTextDetails = dialogView.findViewById(R.id.editTextDetails);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSendReport = dialogView.findViewById(R.id.btnSendReport);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSendReport.setOnClickListener(v -> {
            int selectedRadioId = radioGroupIssues.getCheckedRadioButtonId();

            if (selectedRadioId == -1) {
                // No radio button selected - handle error (e.g., show Toast)
                return;
            }

            int reportStatus = 0;
            if (selectedRadioId == R.id.radioNotDelivered) {
                reportStatus = 1;
            } else if (selectedRadioId == R.id.radioFaulty) {
                reportStatus = 2;
            } else if (selectedRadioId == R.id.radioWrongItem) {
                reportStatus = 3;
            }

            String additionalInfo = editTextDetails.getText().toString().trim();

            orderModel.setReportStatus(reportStatus);
            orderModel.setReportAdditionalInfo(additionalInfo); // Assuming this method exists in OrderModel

            if (listener != null) {
                listener.onReportSubmitted(orderModel, view);
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}