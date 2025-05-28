// File: DiscountStrategy.java
package com.example.foodapp2025.utils.discount;

import com.example.foodapp2025.data.model.VoucherModel;

public interface Discount {
    double applyDiscount(double orderTotal, VoucherModel voucher);
}
