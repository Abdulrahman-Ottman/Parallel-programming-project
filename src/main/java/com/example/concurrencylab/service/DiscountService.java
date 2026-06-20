package com.example.concurrencylab.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
 public class DiscountService {

    private int maxUses = 20;
    private int currentUses = 0;

    private Set<Long> users = new HashSet<>();

    public synchronized boolean applyDiscount(Long userId) {

        if (currentUses < maxUses) {

            try {
                Thread.sleep(50);
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

    public Set<Long> getUsers() {
        return users;
    }
}