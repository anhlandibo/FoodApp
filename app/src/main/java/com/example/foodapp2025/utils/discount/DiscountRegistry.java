// File: DiscountStrategyRegistry.java
package com.example.foodapp2025.utils.discount;

import java.util.HashMap;
import java.util.Map;

public class DiscountRegistry {
    private static final Map<String, Discount> strategies = new HashMap<>();

    static {
        register("percentage", new PercentageDiscount());
        register("fixed", new MoneyDiscount());
        register("shipping", new ShippingDiscount());
    }

    public static void register(String type, Discount strategy) {
        strategies.put(type.toLowerCase(), strategy);
    }

    public static Discount get(String type) {
        return strategies.get(type.toLowerCase());
    }

    public static boolean contains(String type) {
        return strategies.containsKey(type.toLowerCase());
    }
}
