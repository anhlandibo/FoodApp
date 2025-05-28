// File: PercentageDiscountStrategy.java
package com.example.foodapp2025.utils.discount;

import com.example.foodapp2025.data.model.VoucherModel;

public class PercentageDiscount implements Discount {
    @Override
    public double applyDiscount(double orderTotal, VoucherModel voucher) {
        return orderTotal * voucher.getDiscountValue() / 100;
    }
}
