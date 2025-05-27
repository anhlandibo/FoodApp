// File: VoucherViewModel.java
package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.data.repository.VoucherRepository;
import com.example.foodapp2025.utils.discount.Discount;
import com.example.foodapp2025.utils.discount.DiscountRegistry;

import java.util.Date;

public class VoucherViewModel extends ViewModel {
    private final VoucherRepository repository;

    public VoucherViewModel(VoucherRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<String> validateAndApplyVoucher(String code, double orderTotal) {
        MutableLiveData<String> result = new MutableLiveData<>();

        repository.getVoucherByCode(code).observeForever(voucher -> {
            if (voucher == null) {
                result.setValue("Voucher code is invalid.");
                return;
            }

            Date now = new Date();

            if (!voucher.isActive()) {
                result.setValue("Voucher is not active.");
            } else if (voucher.getStartDate() != null && now.before(voucher.getStartDate())) {
                result.setValue("Voucher is not yet valid.");
            } else if (voucher.getExpiryDate() != null && now.after(voucher.getExpiryDate())) {
                result.setValue("Voucher has expired.");
            } else if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                result.setValue("Voucher usage limit reached.");
            } else if (orderTotal < voucher.getMinOrderValue()) {
                result.setValue("Minimum order value not met. Required: $" + voucher.getMinOrderValue());
            } else {
                if (!DiscountRegistry.contains(voucher.getDiscountType())) {
                    result.setValue("Unknown discount type: " + voucher.getDiscountType());
                    return;
                }

                Discount strategy = DiscountRegistry.get(voucher.getDiscountType());
                double discount = strategy.applyDiscount(orderTotal, voucher);
                result.setValue("Voucher applied! Discount: $" + discount);
            }
        });

        return result;
    }
}
