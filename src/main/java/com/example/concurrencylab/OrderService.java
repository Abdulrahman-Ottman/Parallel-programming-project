package com.example.concurrencylab;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private List<String> orders = new ArrayList<>();
    private DiscountService discountService = new DiscountService();

    public String buy(String userId, String productId, String discountCode) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String order = "User: " + userId +
                ", Product: " + productId +
                ", Discount: " + discountApplied;

        orders.add(order);

        return order;
    }

    public List<String> getOrders() {
        return orders;
    }

    public DiscountService getDiscountService() {
        return discountService;
    }
}