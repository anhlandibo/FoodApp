// File: MoneyDiscountStrategy.java
package com.example.foodapp2025.utils.discount;

import com.example.foodapp2025.data.model.VoucherModel;

public class MoneyDiscount implements Discount {
    @Override
    public double applyDiscount(double orderTotal, VoucherModel voucher) {
        return voucher.getDiscountValue();
    }
}
