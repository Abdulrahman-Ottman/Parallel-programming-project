package com.example.concurrencylab.repository;


import com.example.concurrencylab.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}