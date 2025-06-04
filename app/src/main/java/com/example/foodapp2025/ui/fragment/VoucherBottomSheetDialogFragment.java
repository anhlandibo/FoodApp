package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.VoucherModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class VoucherBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_VOUCHER = "arg_voucher";
    private VoucherModel voucher;
    private OnVoucherCancelledListener cancelListener;

    public interface OnVoucherCancelledListener {
        void onCancelled();
    }

    public void setOnVoucherCancelledListener(OnVoucherCancelledListener listener) {
        this.cancelListener = listener;
    }

    public static VoucherBottomSheetDialogFragment newInstance(VoucherModel voucher) {
        VoucherBottomSheetDialogFragment fragment = new VoucherBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VOUCHER, voucher);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_voucher_bottom_sheet, container, false);

        if (getArguments() != null) {
            voucher = (VoucherModel) getArguments().getSerializable(ARG_VOUCHER);
        }

        TextView tvVoucherCode = view.findViewById(R.id.tvVoucherCode);
        TextView tvVoucherDiscount = view.findViewById(R.id.tvVoucherDiscount);
        TextView closeButton = view.findViewById(R.id.closeButton);

        if (voucher != null) {
            tvVoucherCode.setText("Mã: " + voucher.getCode());
            tvVoucherDiscount.setText(voucher.getDiscountType());
        }

        closeButton.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancelled();
            }
            dismiss(); // Đóng bottom sheet
        });

        return view;
    }
}
