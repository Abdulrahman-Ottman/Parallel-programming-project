package com.example.concurrencylab.repository;


import com.example.concurrencylab.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByUserIdAndCreatedAtBetween(Long ID,LocalDateTime start, LocalDateTime end);

}