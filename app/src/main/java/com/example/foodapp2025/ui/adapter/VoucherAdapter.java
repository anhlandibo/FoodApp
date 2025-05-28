package com.example.foodapp2025.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.VoucherModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    public interface OnVoucherClickListener {
        void onVoucherClick(VoucherModel voucher);
    }

    private List<VoucherModel> vouchers;
    private OnVoucherClickListener listener;

    public VoucherAdapter(List<VoucherModel> vouchers, OnVoucherClickListener listener) {
        this.vouchers = vouchers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_voucher , parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        VoucherModel voucher = vouchers.get(position);

        holder.codeText.setText("Mã: " + voucher.getCode());
        holder.descText.setText(voucher.getDescription());

        if ("percentage".equalsIgnoreCase(voucher.getDiscountType())) {
            holder.discountText.setText("Giảm " + (int)voucher.getDiscountValue() + "%");
        } else if ("fixed".equalsIgnoreCase(voucher.getDiscountType())) {
            holder.discountText.setText("Giảm " + (int)voucher.getDiscountValue() + "K");
        } else {
            holder.discountText.setText("Giảm đặc biệt");
        }

        if (voucher.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.expiryText.setText("HSD: " + sdf.format(voucher.getExpiryDate()));
        } else {
            holder.expiryText.setText("Không xác định hạn");
        }

        holder.itemView.setOnClickListener(v -> listener.onVoucherClick(voucher));
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView codeText, descText, discountText, expiryText;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.tvVoucherCode);
            descText = itemView.findViewById(R.id.tvVoucherDescription);
            discountText = itemView.findViewById(R.id.tvVoucherDiscount);
            expiryText = itemView.findViewById(R.id.tvVoucherExpiry);
        }
    }
}
