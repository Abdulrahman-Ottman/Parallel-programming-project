package com.example.concurrencylab;

import java.util.HashMap;
import java.util.Map;

public class FlashSaleService {

    private int stock = 10;
    private Map<String, Integer> reservations = new HashMap<>();
    public int getStock() {
        return stock;
    }

    public Map<String, Integer> getReservations() {
        return reservations;
    }

    public String reserve(String userId) {
        if (stock > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stock--;
            reservations.put(userId, reservations.getOrDefault(userId, 0) + 1);
            return "Reserved!";
        }
        return "Out of stock!";
    }
}