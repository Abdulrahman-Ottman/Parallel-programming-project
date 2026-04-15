package com.example.concurrencylab;

import java.util.HashSet;
import java.util.Set;

public class DiscountService {

    private int maxUses = 10;
    private int currentUses = 0;

    private Set<String> users = new HashSet<>();

    public boolean applyDiscount(String userId) {

        if (currentUses < maxUses) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            currentUses++;
            users.add(userId);

            return true;
        }

        return false;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public Set<String> getUsers() {
        return users;
    }
}